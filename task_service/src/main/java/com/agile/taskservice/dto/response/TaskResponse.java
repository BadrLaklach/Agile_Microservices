package com.agile.taskservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID projectId,
    UUID sprintId,
    String title,
    String description,
    String type,
    String priority,
    String status,
    Integer estimate,
    UUID assigneeId,
    UUID createdBy,
    Instant createdAt,
    Instant updatedAt
) {}
