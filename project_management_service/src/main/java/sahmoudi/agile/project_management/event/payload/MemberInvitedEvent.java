package sahmoudi.agile.project_management.event.payload;

import java.time.Instant;
import java.util.UUID;

// Routing key: member.invited
public record MemberInvitedEvent(
    String  eventType,   // "MEMBER_INVITED"
    UUID    userId,
    UUID    projectId,
    String  role,
    Instant invitedAt
) {}
