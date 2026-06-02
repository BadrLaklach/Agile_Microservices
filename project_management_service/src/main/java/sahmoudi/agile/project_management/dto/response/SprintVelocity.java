package sahmoudi.agile.project_management.dto.response;

import java.util.UUID;

public record SprintVelocity(
        UUID sprintId,
        String sprintName,
        int velocity
) {}
