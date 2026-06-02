package sahmoudi.agile.project_management.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant joinedAt
) {}
