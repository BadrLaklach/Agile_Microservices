package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record TaskCreatedEvent(
    String  eventType,   // "TASK_CREATED"
    UUID    taskId,
    UUID    projectId,
    UUID    sprintId,
    String  title,
    String  type,
    String  status,      // always "TODO"
    Integer estimate,
    UUID    assigneeId,
    Instant createdAt
) {}
