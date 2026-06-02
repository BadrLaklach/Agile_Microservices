package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record TaskDeletedEvent(
    String  eventType,   // "TASK_DELETED"
    UUID    taskId,
    UUID    projectId,
    Instant deletedAt
) {}
