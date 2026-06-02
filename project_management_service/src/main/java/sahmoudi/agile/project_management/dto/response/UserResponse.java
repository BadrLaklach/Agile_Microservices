package sahmoudi.agile.project_management.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the User Service's UserResponse DTO.
 * Used for deserializing responses from UserServiceClient.
 */
public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant createdAt
) {}
