package sahmoudi.agile.project_management.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException() {
        super("User Service is currently unavailable");
    }
}
