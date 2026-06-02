package sahmoudi.agile.project_management.exception;

public class TaskServiceUnavailableException extends RuntimeException {
    public TaskServiceUnavailableException() {
        super("Task Service is currently unavailable");
    }
}
