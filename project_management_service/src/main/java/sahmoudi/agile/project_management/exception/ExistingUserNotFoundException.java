package sahmoudi.agile.project_management.exception;

public class ExistingUserNotFoundException extends RuntimeException {
    public ExistingUserNotFoundException(String email) {
        super("User with email " + email + " was not found. Please verify the email or invite them as a new user.");
    }
}
