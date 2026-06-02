package sahmoudi.agile.project_management.exception;

import java.util.UUID;

public class ActiveSprintExistsException extends RuntimeException {
    public ActiveSprintExistsException(UUID projectId) {
        super("An active sprint already exists in project " + projectId);
    }
}
