package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record TaskUpdatedEvent(
    String  eventType,   // "TASK_UPDATED"
    UUID    taskId,
    UUID    projectId,
    UUID    sprintId,
    String  title,
    String  type,
    String  status,
    Integer estimate,
    UUID    assigneeId,
    Instant updatedAt
) {}
