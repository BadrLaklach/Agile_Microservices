package com.agile.notificationservice.dto;

import java.util.UUID;

public record RecipientDto(
    UUID   userId,
    String email,
    String firstName
) {}
