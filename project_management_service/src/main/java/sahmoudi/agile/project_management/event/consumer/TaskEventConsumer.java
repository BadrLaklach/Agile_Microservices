package sahmoudi.agile.project_management.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sahmoudi.agile.project_management.model.TaskProjection;
import sahmoudi.agile.project_management.repository.TaskProjectionRepository;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskProjectionRepository taskProjectionRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "pm-service.task-events")
    public void handleTaskEvent(Map<String, Object> message) {
        try {
            String eventType = (String) message.get("eventType");
            log.info("Received task event: {}", eventType);

            switch (eventType) {
                case "TASK_CREATED" -> handleTaskCreated(message);
                case "TASK_UPDATED" -> handleTaskUpdated(message);
                case "TASK_STATUS_CHANGED" -> handleTaskStatusChanged(message);
                case "TASK_DELETED" -> handleTaskDeleted(message);
                default -> log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            // Log but do NOT re-throw to avoid infinite requeue
            log.error("Failed to process task event: {}, error: {}", message, e.getMessage(), e);
        }
    }

    private void handleTaskCreated(Map<String, Object> message) {
        TaskProjection projection = TaskProjection.builder()
            .id((String) message.get("taskId"))
            .projectId((String) message.get("projectId"))
            .sprintId((String) message.get("sprintId"))
            .title((String) message.get("title"))
            .type((String) message.get("type"))
            .status((String) message.get("status"))
            .estimate(message.get("estimate") != null ? ((Number) message.get("estimate")).intValue() : null)
            .assigneeId((String) message.get("assigneeId"))
            .createdAt(parseInstant(message.get("createdAt")))
            .updatedAt(parseInstant(message.get("createdAt")))
            .build();
        taskProjectionRepository.save(projection);
        log.info("Upserted task projection: {}", projection.getId());
    }

    private void handleTaskUpdated(Map<String, Object> message) {
        String taskId = (String) message.get("taskId");
        taskProjectionRepository.findById(taskId).ifPresentOrElse(
            existing -> {
                existing.setProjectId((String) message.get("projectId"));
                existing.setSprintId((String) message.get("sprintId"));
                existing.setTitle((String) message.get("title"));
                existing.setType((String) message.get("type"));
                existing.setStatus((String) message.get("status"));
                existing.setEstimate(message.get("estimate") != null ? ((Number) message.get("estimate")).intValue() : null);
                existing.setAssigneeId((String) message.get("assigneeId"));
                existing.setUpdatedAt(parseInstant(message.get("updatedAt")));
                taskProjectionRepository.save(existing);
                log.info("Updated task projection: {}", taskId);
            },
            () -> {
                // If document doesn't exist yet, create it (eventual consistency)
                handleTaskCreated(message);
            }
        );
    }

    private void handleTaskStatusChanged(Map<String, Object> message) {
        String taskId = (String) message.get("taskId");
        taskProjectionRepository.findById(taskId).ifPresentOrElse(
            existing -> {
                existing.setStatus((String) message.get("toStatus"));
                existing.setUpdatedAt(parseInstant(message.get("changedAt")));
                taskProjectionRepository.save(existing);
                log.info("Updated task status: {} -> {}", taskId, message.get("toStatus"));
            },
            () -> log.warn("Task projection not found for status change: {}", taskId)
        );
    }

    private void handleTaskDeleted(Map<String, Object> message) {
        String taskId = (String) message.get("taskId");
        taskProjectionRepository.deleteById(taskId);
        log.info("Deleted task projection: {}", taskId);
    }

    private Instant parseInstant(Object value) {
        if (value == null) return Instant.now();
        if (value instanceof String s) return Instant.parse(s);
        return Instant.now();
    }
}
