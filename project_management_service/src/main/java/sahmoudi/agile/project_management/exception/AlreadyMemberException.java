package sahmoudi.agile.project_management.exception;

import java.util.UUID;

public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(UUID userId, UUID projectId) {
        super("User " + userId + " is already a member of project " + projectId);
    }
}
