package sahmoudi.agile.project_management.dto.response;

import java.util.List;
import java.util.UUID;

public record VelocityResponse(
        UUID projectId,
        double averageVelocity,
        List<SprintVelocity> sprints
) {}
