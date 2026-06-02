package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateSprintRequest(
        @Size(max = 255) String name,
        String goal,
        @Positive Integer capacity,
        LocalDate startDate,
        LocalDate endDate
) {}
