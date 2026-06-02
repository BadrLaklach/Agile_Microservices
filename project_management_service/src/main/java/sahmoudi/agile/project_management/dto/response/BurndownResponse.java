package sahmoudi.agile.project_management.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BurndownResponse(
        UUID sprintId,
        String sprintName,
        LocalDate startDate,
        LocalDate endDate,
        int totalEstimate,
        int remainingEstimate,
        int completedEstimate,
        List<BurndownPoint> idealBurndown
) {}
