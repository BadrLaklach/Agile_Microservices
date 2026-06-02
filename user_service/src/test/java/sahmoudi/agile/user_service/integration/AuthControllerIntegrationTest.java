package sahmoudi.agile.user_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import sahmoudi.agile.user_service.auth.dto.LoginRequest;
import sahmoudi.agile.user_service.auth.dto.RegisterRequest;
import sahmoudi.agile.user_service.user.entity.Role;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/auth";
    }

    @Test
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "newuser@agile.local",
                "securePassword123",
                "New",
                "User",
                Role.DEV
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
        
        List<String> cookies = response.headers().allValues("Set-Cookie");
        assertThat(cookies).isNotEmpty();
        assertThat(cookies.get(0)).contains("accessToken=");
        assertThat(cookies.get(0)).contains("HttpOnly");

        assertThat(response.body()).contains("\"email\":\"newuser@agile.local\"");
        assertThat(response.body()).contains("\"role\":\"DEV\"");
    }

    @Test
    void register_EmailAlreadyExists_Returns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "admin@agile.local", // This user is pre-seeded
                "securePassword123",
                "Another",
                "Admin",
                Role.ADMIN
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.body()).contains("Email already exists");
    }

    @Test
    void register_ValidationFails_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "short",
                "",
                "",
                Role.DEV
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Constraint violation");
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("po@agile.local", "password123");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        
        List<String> cookies = response.headers().allValues("Set-Cookie");
        assertThat(cookies).isNotEmpty();
        assertThat(cookies.get(0)).contains("accessToken=");

        assertThat(response.body()).contains("\"email\":\"po@agile.local\"");
        assertThat(response.body()).contains("\"role\":\"PO\"");
    }

    @Test
    void login_BadPassword_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin@agile.local", "wrongpassword");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("Invalid credentials");
    }

    @Test
    void login_UserNotFound_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@agile.local", "password123");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body()).contains("Invalid credentials");
    }
}
