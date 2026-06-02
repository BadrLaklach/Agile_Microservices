package com.agile.taskservice.dto.response;

import java.util.List;

public record PagedTaskResponse(
    List<TaskResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
