package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;

public record TaskDeletedEvent(
        String eventType,
        String taskId,
        String projectId,
        Instant deletedAt
) {}
