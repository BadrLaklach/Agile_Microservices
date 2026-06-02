package sahmoudi.agile.user_service.user.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sahmoudi.agile.user_service.auth.service.JwtService;
import sahmoudi.agile.user_service.user.dto.UserResponse;
import sahmoudi.agile.user_service.user.service.UserService;
import sahmoudi.agile.user_service.auth.exception.UnauthorizedTokenException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @CookieValue(name = "accessToken", required = false) String token
    ) {
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedTokenException();
        }
        return ResponseEntity.ok(userService.getAuthenticatedUser(token));
    }

    /**
     * Get user by ID. Supports two authentication modes:
     * 1. Cookie-based JWT (browser/frontend calls)
     * 2. Header-based identity (inter-service calls from API Gateway)
     *
     * If X-User-Id and X-User-Role headers are present, the request is
     * treated as an authenticated inter-service call (the API Gateway
     * has already validated the JWT and forwarded claims as headers).
     *
     * If those headers are absent, falls back to cookie-based JWT auth.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id,
            @CookieValue(name = "accessToken", required = false) String token,
            @RequestHeader(name = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(name = "X-User-Role", required = false) String headerUserRole
    ) {
        // Header-based auth (inter-service calls)
        if (headerUserId != null && !headerUserId.isBlank()
                && headerUserRole != null && !headerUserRole.isBlank()) {
            return ResponseEntity.ok(userService.getUserById(id));
        }

        // Cookie-based auth (direct browser calls)
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedTokenException();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email,
            @CookieValue(name = "accessToken", required = false) String token,
            @RequestHeader(name = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(name = "X-User-Role", required = false) String headerUserRole
    ) {
        if (headerUserId != null && !headerUserId.isBlank()
                && headerUserRole != null && !headerUserRole.isBlank()) {
            return ResponseEntity.ok(userService.getUserByEmail(email));
        }

        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedTokenException();
        }
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}
