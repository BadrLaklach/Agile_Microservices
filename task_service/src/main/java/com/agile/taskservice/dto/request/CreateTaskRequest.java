package com.agile.taskservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTaskRequest(
    @NotBlank @Size(max = 255) String title,
    String description,
    @NotNull String type,
    String priority,
    UUID projectId,
    UUID sprintId,
    @Positive Integer estimate,
    UUID assigneeId
) {}
