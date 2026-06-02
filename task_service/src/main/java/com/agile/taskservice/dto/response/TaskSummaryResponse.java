package com.agile.taskservice.dto.response;

import java.util.Map;
import java.util.UUID;

public record TaskSummaryResponse(
    UUID projectId,
    UUID sprintId,
    long total,
    Map<String, Long> byStatus,
    Map<String, Long> byType
) {}
