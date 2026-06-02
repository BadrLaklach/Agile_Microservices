package sahmoudi.agile.project_management.exception;

import java.util.UUID;

public class SprintNotFoundException extends RuntimeException {
    public SprintNotFoundException(UUID id) {
        super("Sprint not found: " + id);
    }
}
