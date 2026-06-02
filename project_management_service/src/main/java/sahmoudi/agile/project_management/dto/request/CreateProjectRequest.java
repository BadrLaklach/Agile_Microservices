package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull @Pattern(regexp = "SCRUM|KANBAN|HYBRID") String methodology,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
