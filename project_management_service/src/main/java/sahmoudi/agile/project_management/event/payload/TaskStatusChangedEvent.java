package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;

public record TaskStatusChangedEvent(
        String eventType,
        String taskId,
        String projectId,
        String sprintId,
        String fromStatus,
        String toStatus,
        Instant changedAt
) {}
