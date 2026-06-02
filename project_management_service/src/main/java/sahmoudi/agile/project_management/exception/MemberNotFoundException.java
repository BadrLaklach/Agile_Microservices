package sahmoudi.agile.project_management.exception;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(UUID userId, UUID projectId) {
        super("Member not found: " + userId + " in project " + projectId);
    }
}
