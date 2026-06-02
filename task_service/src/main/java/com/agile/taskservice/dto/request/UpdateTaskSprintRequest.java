package com.agile.taskservice.dto.request;

import java.util.UUID;

public record UpdateTaskSprintRequest(
    UUID sprintId
) {}
