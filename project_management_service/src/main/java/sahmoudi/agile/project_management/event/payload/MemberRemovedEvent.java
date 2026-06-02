package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;
import java.util.UUID;

// Routing key: member.removed
public record MemberRemovedEvent(
    String  eventType,   // "MEMBER_REMOVED"
    UUID    userId,
    UUID    projectId,
    Instant removedAt
) {}
