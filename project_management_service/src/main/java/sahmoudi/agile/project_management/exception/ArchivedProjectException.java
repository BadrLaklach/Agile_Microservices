package sahmoudi.agile.project_management.exception;

import java.util.UUID;

public class ArchivedProjectException extends RuntimeException {
    public ArchivedProjectException(UUID projectId) {
        super("Project " + projectId + " is archived");
    }
}
