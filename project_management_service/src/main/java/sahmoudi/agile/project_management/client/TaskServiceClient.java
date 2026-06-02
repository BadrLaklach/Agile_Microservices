package sahmoudi.agile.project_management.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sahmoudi.agile.project_management.dto.request.UpdateTaskSprintRequest;
import sahmoudi.agile.project_management.exception.TaskNotFoundException;
import sahmoudi.agile.project_management.exception.TaskServiceUnavailableException;
import java.util.UUID;

@Slf4j
@Component
public class TaskServiceClient {

    private final RestClient restClient;

    public TaskServiceClient(@Value("${app.task-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void updateTaskSprint(UUID taskId, UUID sprintId, UUID callerId, String callerRole) {
        try {
            restClient.patch()
                .uri("/api/v1/tasks/{id}/sprint", taskId)
                .header("X-User-Id", callerId.toString())
                .header("X-User-Role", callerRole)
                .body(new UpdateTaskSprintRequest(sprintId))
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new TaskNotFoundException(taskId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new TaskServiceUnavailableException();
                })
                .toBodilessEntity();
        } catch (TaskNotFoundException | TaskServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call Task Service: {}", e.getMessage());
            throw new TaskServiceUnavailableException();
        }
    }
}
