package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record MemberInvitedEvent(
    String  eventType,   // "MEMBER_INVITED"
    UUID    userId,
    UUID    projectId,
    String  role,
    Instant invitedAt
) {}
