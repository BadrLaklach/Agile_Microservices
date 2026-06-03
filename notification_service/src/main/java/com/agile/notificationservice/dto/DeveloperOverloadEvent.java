package com.agile.notificationservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeveloperOverloadEvent(
    String             eventType,
    UUID               projectId,
    String             projectName,
    UUID               sprintId,
    String             sprintName,
    UUID               developerId,
    String             developerFirstName,
    String             developerLastName,
    int                developerLoad,
    int                developerCapacity,
    double             loadRate,
    List<RecipientDto> recipients,
    Instant            occurredAt
) {}
