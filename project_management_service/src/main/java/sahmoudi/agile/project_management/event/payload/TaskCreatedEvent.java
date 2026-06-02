package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;

public record TaskCreatedEvent(
        String eventType,
        String taskId,
        String projectId,
        String sprintId,
        String title,
        String type,
        String status,
        Integer estimate,
        String assigneeId,
        Instant createdAt
) {}
