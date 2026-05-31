package sahmoudi.agile.user_service.auth.exception;

public class UnauthorizedTokenException extends RuntimeException {
    public UnauthorizedTokenException() {
        super("Access token is missing or invalid");
    }
}
