package sahmoudi.agile.project_management.dto.response;

import java.util.UUID;

public record TaskProjectionResponse(
        UUID taskId,
        String title,
        String type,
        String status,
        Integer estimate,
        UUID assigneeId
) {}
