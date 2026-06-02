package sahmoudi.agile.project_management.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SprintTaskResponse(
        UUID taskId,
        UUID sprintId,
        Instant assignedAt
) {}
