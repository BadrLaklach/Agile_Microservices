package sahmoudi.agile.project_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sahmoudi.agile.project_management.client.TaskServiceClient;
import sahmoudi.agile.project_management.dto.request.AssignTaskRequest;
import sahmoudi.agile.project_management.dto.request.CreateSprintRequest;
import sahmoudi.agile.project_management.dto.request.UpdateSprintRequest;
import sahmoudi.agile.project_management.dto.response.SprintResponse;
import sahmoudi.agile.project_management.dto.response.SprintTaskResponse;
import sahmoudi.agile.project_management.dto.response.TaskProjectionResponse;
import sahmoudi.agile.project_management.exception.*;
import sahmoudi.agile.project_management.model.*;
import sahmoudi.agile.project_management.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository memberRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final TaskProjectionRepository taskProjectionRepository;
    private final TaskServiceClient taskServiceClient;

    private void requireRole(String callerRole, String... allowedRoles) {
        if (!Set.of(allowedRoles).contains(callerRole)) {
            throw new AccessDeniedException();
        }
    }

    private void requireMembership(UUID projectId, UUID callerId, String callerRole) {
        if ("ADMIN".equals(callerRole)) return;
        if (!memberRepository.existsByProjectIdAndUserId(projectId, callerId)) {
            throw new AccessDeniedException();
        }
    }

    @Transactional
    public SprintResponse createSprint(UUID projectId, CreateSprintRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        if ("ARCHIVED".equals(project.getStatus())) {
            throw new ArchivedProjectException(projectId);
        }

        Sprint sprint = Sprint.builder()
            .projectId(projectId)
            .name(request.name())
            .goal(request.goal())
            .status("PLANNED")
            .capacity(request.capacity())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .build();
        sprint = sprintRepository.save(sprint);
        return toResponse(sprint);
    }

    public List<SprintResponse> listSprints(UUID projectId, String status, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        List<Sprint> sprints;
        if (status != null && !status.isBlank()) {
            sprints = sprintRepository.findByProjectIdAndStatus(projectId, status);
        } else {
            sprints = sprintRepository.findByProjectId(projectId);
        }
        return sprints.stream().map(this::toResponse).toList();
    }

    public SprintResponse getSprint(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID projectId, UUID sprintId, UpdateSprintRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if ("COMPLETED".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Completed sprints cannot be updated");
        }

        if (request.name() != null) sprint.setName(request.name());
        if (request.goal() != null) sprint.setGoal(request.goal());
        if (request.capacity() != null) sprint.setCapacity(request.capacity());
        if (request.startDate() != null) sprint.setStartDate(request.startDate());
        if (request.endDate() != null) sprint.setEndDate(request.endDate());

        sprint = sprintRepository.save(sprint);
        return toResponse(sprint);
    }

    @Transactional
    public void deleteSprint(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if (!"PLANNED".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Only PLANNED sprints can be deleted");
        }

        sprintRepository.delete(sprint);
    }

    @Transactional
    public SprintResponse startSprint(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if (!"PLANNED".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Only PLANNED sprints can be started");
        }

        if (sprintRepository.existsByProjectIdAndStatus(projectId, "ACTIVE")) {
            throw new ActiveSprintExistsException(projectId);
        }

        sprint.setStatus("ACTIVE");
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }
        sprint = sprintRepository.save(sprint);
        return toResponse(sprint);
    }

    @Transactional
    public SprintResponse closeSprint(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if (!"ACTIVE".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Only ACTIVE sprints can be closed");
        }

        sprint.setStatus("COMPLETED");
        if (sprint.getEndDate() == null) {
            sprint.setEndDate(LocalDate.now());
        }
        sprint = sprintRepository.save(sprint);
        return toResponse(sprint);
    }

    // --- Sprint Task Assignment ---

    @Transactional
    public SprintTaskResponse assignTask(UUID projectId, UUID sprintId, AssignTaskRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if ("COMPLETED".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Cannot assign tasks to a completed sprint");
        }

        if (sprintTaskRepository.existsBySprintIdAndTaskId(sprintId, request.taskId())) {
            throw new AlreadyMemberException(request.taskId(), sprintId); // reusing for conflict
        }

        // Call Task Service FIRST — synchronous HTTP
        taskServiceClient.updateTaskSprint(request.taskId(), sprintId, callerId, callerRole);

        // Only after Task Service acknowledges, persist locally
        SprintTask sprintTask = SprintTask.builder()
            .sprintId(sprintId)
            .taskId(request.taskId())
            .build();
        sprintTask = sprintTaskRepository.save(sprintTask);

        return new SprintTaskResponse(request.taskId(), sprintId, sprintTask.getAssignedAt());
    }

    @Transactional
    public void removeTask(UUID projectId, UUID sprintId, UUID taskId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO", "SM");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        if ("COMPLETED".equals(sprint.getStatus())) {
            throw new InvalidSprintStateException("Cannot remove tasks from a completed sprint");
        }

        SprintTaskId stId = new SprintTaskId(sprintId, taskId);
        if (!sprintTaskRepository.existsById(stId)) {
            throw new TaskNotFoundException(taskId);
        }

        // Call Task Service FIRST
        taskServiceClient.updateTaskSprint(taskId, null, callerId, callerRole);

        // Only after success, delete locally
        sprintTaskRepository.deleteById(stId);
    }

    public List<TaskProjectionResponse> listSprintTasks(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        // Read from MongoDB CQRS read model
        return taskProjectionRepository.findBySprintId(sprintId.toString())
            .stream()
            .map(tp -> new TaskProjectionResponse(
                UUID.fromString(tp.getId()),
                tp.getTitle(),
                tp.getType(),
                tp.getStatus(),
                tp.getEstimate(),
                tp.getAssigneeId() != null ? UUID.fromString(tp.getAssigneeId()) : null
            ))
            .toList();
    }

    private SprintResponse toResponse(Sprint s) {
        return new SprintResponse(
            s.getId(), s.getProjectId(), s.getName(), s.getGoal(),
            s.getStatus(), s.getCapacity(), s.getStartDate(), s.getEndDate(),
            s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
