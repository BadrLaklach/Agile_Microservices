package com.agile.taskservice.event.payload;

import java.time.Instant;
import java.util.UUID;

public record MemberRemovedEvent(
    String  eventType,   // "MEMBER_REMOVED"
    UUID    userId,
    UUID    projectId,
    Instant removedAt
) {}
