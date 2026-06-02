package sahmoudi.agile.project_management.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String methodology,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
