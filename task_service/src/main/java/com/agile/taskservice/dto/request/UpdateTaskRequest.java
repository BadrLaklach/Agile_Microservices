package com.agile.taskservice.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTaskRequest(
    @Size(max = 255) String title,
    String description,
    String priority,
    @Positive Integer estimate,
    UUID assigneeId
) {}
