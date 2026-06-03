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
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import sahmoudi.agile.project_management.model.Sprint;
import sahmoudi.agile.project_management.model.Project;
import sahmoudi.agile.project_management.model.ProjectMember;
import sahmoudi.agile.project_management.repository.SprintRepository;
import sahmoudi.agile.project_management.client.UserServiceClient;
import sahmoudi.agile.project_management.repository.ProjectMemberRepository;
import sahmoudi.agile.project_management.repository.ProjectRepository;
import sahmoudi.agile.project_management.event.publisher.EventPublisher;
import sahmoudi.agile.project_management.event.payload.DeveloperOverloadEvent;
import sahmoudi.agile.project_management.event.payload.SprintOverloadEvent;
import sahmoudi.agile.project_management.event.payload.RecipientDto;
import sahmoudi.agile.project_management.dto.response.UserResponse;
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventConsumer {
    private static final UUID SYSTEM_CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final TaskProjectionRepository taskProjectionRepository;
    private final SprintRepository sprintRepository;
    private final UserServiceClient userServiceClient;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final EventPublisher eventPublisher;
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
        checkAndPublishDeveloperOverload(projection);
        checkAndPublishSprintOverload(projection);
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
                checkAndPublishDeveloperOverload(existing);
                checkAndPublishSprintOverload(existing);
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

    private void checkAndPublishSprintOverload(TaskProjection updatedProjection) {
        if (updatedProjection.getSprintId() == null || updatedProjection.getProjectId() == null) {
            return;
        }
        UUID sprintId = UUID.fromString(updatedProjection.getSprintId());
        UUID projectId = UUID.fromString(updatedProjection.getProjectId());

        Sprint sprint = sprintRepository.findById(sprintId).orElse(null);
        if (sprint == null || sprint.getCapacity() == null || sprint.getCapacity() <= 0) return;

        List<TaskProjection> sprintTasks = taskProjectionRepository.findBySprintId(sprintId.toString());
        int totalLoad = sprintTasks.stream()
            .mapToInt(t -> t.getEstimate() != null ? t.getEstimate() : 0)
            .sum();

        if (totalLoad <= sprint.getCapacity()) return;

        double loadRate = ((double) totalLoad / sprint.getCapacity()) * 100;

        List<RecipientDto> recipients = resolveRecipients(
            projectId, List.of("SM", "PO"), SYSTEM_CALLER_ID, "ADMIN");

        if (recipients.isEmpty()) return;

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return;

        eventPublisher.publishSprintOverloadAlert(new SprintOverloadEvent(
            "SPRINT_OVERLOAD",
            projectId, project.getName(),
            sprintId, sprint.getName(),
            sprint.getCapacity(), totalLoad, loadRate,
            recipients,
            Instant.now()
        ));
    }

    private void checkAndPublishDeveloperOverload(TaskProjection updatedProjection) {
        if (updatedProjection.getSprintId() == null || updatedProjection.getAssigneeId() == null || updatedProjection.getProjectId() == null) {
            return;
        }
        UUID sprintId    = UUID.fromString(updatedProjection.getSprintId());
        UUID assigneeId  = UUID.fromString(updatedProjection.getAssigneeId());
        UUID projectId   = UUID.fromString(updatedProjection.getProjectId());

        Sprint sprint = sprintRepository.findById(sprintId).orElse(null);
        if (sprint == null || sprint.getCapacity() == null || sprint.getCapacity() <= 0) return;

        // Sum this developer's total load in the sprint
        List<TaskProjection> devTasks = taskProjectionRepository
            .findAllBySprintIdAndAssigneeId(sprintId.toString(), assigneeId.toString());

        int developerLoad = devTasks.stream()
            .mapToInt(t -> t.getEstimate() != null ? t.getEstimate() : 0)
            .sum();

        // Per-developer capacity = sprint capacity / distinct assignees in sprint
        long distinctAssignees = taskProjectionRepository
            .countDistinctAssigneesBySprintId(sprintId.toString());

        if (distinctAssignees == 0) return;

        int developerCapacity = (int) (sprint.getCapacity() / distinctAssignees);
        if (developerCapacity <= 0) return;

        double loadRate = ((double) developerLoad / developerCapacity) * 100;
        if (loadRate <= 100.0) return;

        // Resolve recipients: the developer + SM members of the project
        List<RecipientDto> recipients = new ArrayList<>();

        // Add the overloaded developer
        try {
            UserResponse dev = userServiceClient.getUserById(
                assigneeId, SYSTEM_CALLER_ID, "ADMIN");
            recipients.add(new RecipientDto(
                assigneeId, dev.email(), dev.firstName()));
        } catch (Exception e) {
            log.warn("Could not resolve developer {}: {}", assigneeId, e.getMessage());
        }

        // Add SM members
        recipients.addAll(resolveRecipients(projectId, List.of("SM"),
            SYSTEM_CALLER_ID, "ADMIN"));

        if (recipients.isEmpty()) return;

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return;

        // Resolve developer name
        String devFirstName = recipients.stream()
            .filter(r -> r.userId().equals(assigneeId))
            .map(RecipientDto::firstName).findFirst().orElse("Unknown");

        eventPublisher.publishDeveloperOverloadAlert(new DeveloperOverloadEvent(
            "DEVELOPER_OVERLOAD",
            projectId, project.getName(),
            sprintId,  sprint.getName(),
            assigneeId, devFirstName, "",
            developerLoad, developerCapacity, loadRate,
            recipients,
            Instant.now()
        ));
    }

    private List<RecipientDto> resolveRecipients(UUID projectId,
                                                 List<String> roles,
                                                 UUID callerId,
                                                 String callerRole) {
        List<RecipientDto> recipients = new ArrayList<>();

        List<ProjectMember> members = projectMemberRepository
            .findAllByProjectIdAndRoleIn(projectId, roles);

        for (ProjectMember member : members) {
            try {
                UserResponse user = userServiceClient.getUserById(
                    member.getUserId(), callerId, callerRole);
                recipients.add(new RecipientDto(
                    member.getUserId(), user.email(), user.firstName()));
            } catch (Exception e) {
                log.warn("Could not resolve recipient {}: {}",
                    member.getUserId(), e.getMessage());
            }
        }
        return recipients;
    }
}
