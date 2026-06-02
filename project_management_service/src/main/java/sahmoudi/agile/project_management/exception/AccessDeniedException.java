package sahmoudi.agile.project_management.exception;

/**
 * Custom AccessDeniedException for authorization failures.
 * This is NOT Spring's AccessDeniedException — it's specific to PM Service
 * role/membership checks.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("Access denied");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
