package sahmoudi.agile.project_management.dto.response;

import java.time.LocalDate;

public record BurndownPoint(
        LocalDate date,
        double ideal
) {}
