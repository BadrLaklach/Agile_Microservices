package com.agile.taskservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull String status
) {}
