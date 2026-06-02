package sahmoudi.agile.project_management.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTaskRequest(
        @NotNull UUID taskId
) {}
