package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record TaskStatusChangedEvent(
    String  eventType,   // "TASK_STATUS_CHANGED"
    UUID    taskId,
    UUID    projectId,
    UUID    sprintId,
    String  fromStatus,
    String  toStatus,
    Instant changedAt
) {}
