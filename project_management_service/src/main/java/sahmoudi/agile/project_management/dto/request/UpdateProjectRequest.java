package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjectRequest(
        @Size(max = 255) String name,
        String description,
        @Pattern(regexp = "SCRUM|KANBAN|HYBRID") String methodology,
        LocalDate startDate,
        LocalDate endDate
) {}
