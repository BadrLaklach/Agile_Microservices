package sahmoudi.agile.project_management.event.payload;

import java.util.UUID;

public record RecipientDto(
    UUID   userId,
    String email,
    String firstName
) {}
