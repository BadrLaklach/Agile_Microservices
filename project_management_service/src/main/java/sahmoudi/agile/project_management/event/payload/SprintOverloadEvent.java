package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SprintOverloadEvent(
    String             eventType,
    UUID               projectId,
    String             projectName,
    UUID               sprintId,
    String             sprintName,
    int                sprintCapacity,
    int                currentLoad,
    double             loadRate,
    List<RecipientDto> recipients,
    Instant            occurredAt
) {}
