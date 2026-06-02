package sahmoudi.agile.project_management.dto.request;

import java.util.UUID;

public record NotificationRequest(
        String eventType,
        UUID targetUserId,
        UUID projectId,
        String message
) {}
