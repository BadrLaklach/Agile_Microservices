package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateSprintRequest(
        @NotBlank @Size(max = 255) String name,
        String goal,
        @Positive Integer capacity,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
