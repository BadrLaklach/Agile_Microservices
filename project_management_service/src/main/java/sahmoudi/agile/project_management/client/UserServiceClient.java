package sahmoudi.agile.project_management.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sahmoudi.agile.project_management.dto.response.UserResponse;
import sahmoudi.agile.project_management.exception.UserNotFoundException;
import sahmoudi.agile.project_management.exception.UserServiceUnavailableException;
import java.util.UUID;

@Slf4j
@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${app.user-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public UserResponse getUserById(UUID userId, UUID callerId, String callerRole) {
        try {
            return restClient.get()
                .uri("/api/v1/users/{id}", userId)
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", callerRole)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new UserNotFoundException(userId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new UserServiceUnavailableException();
                })
                .body(UserResponse.class);
        } catch (UserNotFoundException | UserServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call User Service: {}", e.getMessage());
            throw new UserServiceUnavailableException();
        }
    }
    public UserResponse getUserByEmail(String email, UUID callerId, String callerRole) {
        try {
            return restClient.get()
                .uri("/api/v1/users/email/{email}", email)
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", callerRole)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new UserNotFoundException("User not found with email: " + email);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new UserServiceUnavailableException();
                })
                .body(UserResponse.class);
        } catch (UserNotFoundException | UserServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call User Service for email lookup: {}", e.getMessage());
            throw new UserServiceUnavailableException();
        }
    }

    public UserResponse registerUser(String email, String password, String firstName, String lastName, String role) {
        try {
            java.util.Map<String, String> body = java.util.Map.of(
                    "email", email,
                    "password", password,
                    "firstName", firstName,
                    "lastName", lastName,
                    "role", role
            );

            return restClient.post()
                .uri("/api/v1/auth/register")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new IllegalArgumentException("Invalid registration data");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new UserServiceUnavailableException();
                })
                .body(UserResponse.class);
        } catch (IllegalArgumentException | UserServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call User Service for registration: {}", e.getMessage());
            throw new UserServiceUnavailableException();
        }
    }
}
