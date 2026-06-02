package sahmoudi.agile.user_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import sahmoudi.agile.user_service.auth.dto.LoginRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String validTokenCookie;
    private String adminUserId;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest loginRequest = new LoginRequest("sm@agile.local", "password123");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        List<String> cookies = response.headers().allValues("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            this.validTokenCookie = cookies.get(0).split(";")[0]; // extract token
        }
        
        JsonNode jsonNode = objectMapper.readTree(response.body());
        this.adminUserId = jsonNode.get("id").asText();
    }

    @Test
    void getMe_WithValidCookie_ReturnsProfile() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/me"))
                .header("Cookie", validTokenCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":\"" + adminUserId + "\"");
        assertThat(response.body()).contains("\"email\":\"sm@agile.local\"");
        assertThat(response.body()).contains("\"role\":\"SM\"");
    }

    @Test
    void getMe_WithoutCookie_Returns401() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/me"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("Unauthorized");
    }

    @Test
    void getUserById_WithValidCookie_ReturnsProfile() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/" + adminUserId))
                .header("Cookie", validTokenCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":\"" + adminUserId + "\"");
        assertThat(response.body()).contains("\"email\":\"sm@agile.local\"");
    }

    @Test
    void getUserById_WithInterServiceHeaders_ReturnsProfile() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/" + adminUserId))
                .header("X-User-Id", adminUserId)
                .header("X-User-Role", "SM")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":\"" + adminUserId + "\"");
        assertThat(response.body()).contains("\"email\":\"sm@agile.local\"");
    }

    @Test
    void getUserById_MissingAuth_Returns401() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/" + adminUserId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void getUserById_InvalidUUID_Returns400() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/invalid-uuid"))
                .header("Cookie", validTokenCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Invalid path variable");
    }

    @Test
    void getUserById_UserNotFound_Returns404() throws Exception {
        String randomId = UUID.randomUUID().toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/users/" + randomId))
                .header("Cookie", validTokenCookie)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("User not found");
    }
}
