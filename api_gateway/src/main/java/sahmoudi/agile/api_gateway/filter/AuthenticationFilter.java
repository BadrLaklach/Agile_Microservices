package sahmoudi.agile.api_gateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import sahmoudi.agile.api_gateway.config.JwtUtil;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 0. Bypass authentication for Swagger UI and OpenAPI documentation
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars")) {
            return chain.filter(exchange);
        }

        // 1. Reject requests that prematurely contain X-User-Id or X-User-Role headers
        if (request.getHeaders().containsKey("X-User-Id") || request.getHeaders().containsKey("X-User-Role")) {
            return sendProblemDetailResponse(exchange, HttpStatus.BAD_REQUEST, "Spoofing attempt: invalid headers present");
        }

        String token = null;
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            }
        }
        
        if (token == null) {
            HttpCookie cookie = request.getCookies().getFirst("accessToken");
            if (cookie != null) {
                token = cookie.getValue();
            }
        }

        ServerHttpRequest modifiedRequest = request;

        if (token != null) {
            if (!jwtUtil.isTokenValid(token)) {
                return sendProblemDetailResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired access token");
            }

            try {
                Claims claims = jwtUtil.getAllClaimsFromToken(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();
            } catch (Exception e) {
                return sendProblemDetailResponse(exchange, HttpStatus.UNAUTHORIZED, "Failed to parse access token");
            }
        }

        // 2. Strip X-User-Id and X-User-Role from response before sending back to client
        exchange.getResponse().beforeCommit(() -> Mono.fromRunnable(() -> {
            exchange.getResponse().getHeaders().remove("X-User-Id");
            exchange.getResponse().getHeaders().remove("X-User-Role");
        }));

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> sendProblemDetailResponse(ServerWebExchange exchange, HttpStatus status, String detail) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String path = exchange.getRequest().getURI().getPath();
        String json = String.format(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"instance\":\"%s\"}",
                status.getReasonPhrase(),
                status.value(),
                detail,
                path
        );

        byte[] bytes = json.getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1; // Executed before routing
    }
}
