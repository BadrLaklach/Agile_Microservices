package com.agile.taskservice.service;

import com.agile.taskservice.dto.request.CreateTaskRequest;
import com.agile.taskservice.dto.request.UpdateTaskRequest;
import com.agile.taskservice.dto.request.UpdateTaskSprintRequest;
import com.agile.taskservice.dto.request.UpdateTaskStatusRequest;
import com.agile.taskservice.dto.response.PagedTaskResponse;
import com.agile.taskservice.dto.response.TaskResponse;
import com.agile.taskservice.dto.response.TaskSummaryResponse;
import com.agile.taskservice.event.payload.*;
import com.agile.taskservice.event.publisher.TaskEventPublisher;
import com.agile.taskservice.exception.*;
import com.agile.taskservice.model.MemberProjectView;
import com.agile.taskservice.model.Task;
import com.agile.taskservice.repository.MemberProjectViewRepository;
import com.agile.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MemberProjectViewRepository memberRepository;
    private final TaskEventPublisher eventPublisher;

    private static final Set<String> PROJECT_TASK_TYPES = Set.of("USER_STORY", "BUG", "TECHNICAL_TASK");
    private static final Set<String> OUT_OF_PROJECT_TASK_TYPES = Set.of("SUPPORT", "MAINTENANCE", "MEETING", "TRAINING", "ON_CALL", "LEAVE");

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UUID callerId, String callerRole) {
        validateTaskType(request.type());
        validateTypeProjectConsistency(request.type(), request.projectId());

        if ("MA".equals(callerRole)) {
            throw new AccessDeniedException("MA cannot create tasks");
        }

        if (request.projectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, request.projectId())) {
                throw new NotProjectMemberException();
            }
        }

        String priority = request.priority() != null ? request.priority() : "MEDIUM";
        
        Task task = Task.builder()
                .projectId(request.projectId())
                .sprintId(request.sprintId())
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .priority(priority)
                .status("TODO")
                .estimate(request.estimate())
                .assigneeId(request.assigneeId())
                .createdBy(callerId)
                .build();

        Task saved = taskRepository.save(task);

        eventPublisher.publishCreated(new TaskCreatedEvent(
                "TASK_CREATED", saved.getId(), saved.getProjectId(), saved.getSprintId(),
                saved.getTitle(), saved.getType(), saved.getStatus(), saved.getEstimate(),
                saved.getAssigneeId(), Instant.now()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID id, UUID callerId, String callerRole) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getProjectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, task.getProjectId())) {
                throw new NotProjectMemberException();
            }
        }

        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public PagedTaskResponse listTasks(String projectIdStr, String sprintIdStr, UUID assigneeId, String status,
                                       String type, String priority, int page, int size, UUID callerId, String callerRole) {
        if (size > 100) {
            throw new InvalidQueryParameterException("Maximum page size is 100");
        }
        
        if (status != null && !Set.of("TODO", "IN_PROGRESS", "DONE").contains(status)) {
            throw new InvalidQueryParameterException("Invalid status value: '" + status + "'. Must be one of: TODO, IN_PROGRESS, DONE");
        }

        if (type != null) {
            validateTaskType(type);
        }

        if (priority != null && !Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(priority)) {
            throw new InvalidQueryParameterException("Invalid priority");
        }

        UUID projectId = null;
        boolean filterProjectNull = false;
        if (projectIdStr != null) {
            if ("none".equals(projectIdStr)) {
                filterProjectNull = true;
            } else {
                try {
                    projectId = UUID.fromString(projectIdStr);
                } catch (IllegalArgumentException e) {
                    throw new InvalidQueryParameterException("Invalid projectId UUID");
                }
            }
        }

        UUID sprintId = null;
        boolean filterSprintNull = false;
        if (sprintIdStr != null) {
            if ("none".equals(sprintIdStr)) {
                filterSprintNull = true;
            } else {
                try {
                    sprintId = UUID.fromString(sprintIdStr);
                } catch (IllegalArgumentException e) {
                    throw new InvalidQueryParameterException("Invalid sprintId UUID");
                }
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage;

        if ("ADMIN".equals(callerRole)) {
            taskPage = taskRepository.findAllFiltered(projectId, filterProjectNull, sprintId, filterSprintNull, assigneeId, status, type, priority, pageable);
        } else if (projectId != null) {
            if (!memberRepository.isMember(callerId, projectId)) {
                throw new NotProjectMemberException();
            }
            taskPage = taskRepository.findAllFiltered(projectId, filterProjectNull, sprintId, filterSprintNull, assigneeId, status, type, priority, pageable);
        } else if (filterProjectNull) {
            taskPage = taskRepository.findAllFiltered(null, true, sprintId, filterSprintNull, assigneeId, status, type, priority, pageable);
        } else {
            List<MemberProjectView> memberships = memberRepository.findAllByUserId(callerId);
            List<UUID> callerProjectIds = memberships.stream().map(MemberProjectView::getProjectId).toList();

            if (callerProjectIds.isEmpty()) {
                return new PagedTaskResponse(List.of(), page, size, 0, 0);
            }
            taskPage = taskRepository.findAllFilteredWithProjectIds(callerProjectIds, sprintId, filterSprintNull, assigneeId, status, type, priority, pageable);
        }

        List<TaskResponse> content = taskPage.getContent().stream().map(this::toResponse).toList();
        return new PagedTaskResponse(content, taskPage.getNumber(), taskPage.getSize(), taskPage.getTotalElements(), taskPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public TaskSummaryResponse getSummary(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        if (projectId == null && sprintId == null) {
            throw new InvalidQueryParameterException("At least one of projectId or sprintId must be provided");
        }

        if (projectId != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, projectId)) {
                throw new NotProjectMemberException();
            }
        }

        List<Object[]> byStatusRaw = taskRepository.countByStatus(projectId, sprintId);
        List<Object[]> byTypeRaw = taskRepository.countByType(projectId, sprintId);

        Map<String, Long> byStatus = new HashMap<>();
        long total = 0;
        for (Object[] row : byStatusRaw) {
            byStatus.put((String) row[0], ((Number) row[1]).longValue());
            total += ((Number) row[1]).longValue();
        }

        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : byTypeRaw) {
            byType.put((String) row[0], ((Number) row[1]).longValue());
        }

        return new TaskSummaryResponse(projectId, sprintId, total, byStatus, byType);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request, UUID callerId, String callerRole) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getProjectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, task.getProjectId())) {
                throw new NotProjectMemberException();
            }
        }

        if ("MA".equals(callerRole)) {
            throw new AccessDeniedException("MA cannot update tasks");
        }

        if ("DEV".equals(callerRole)) {
            if (!callerId.equals(task.getAssigneeId()) && !callerId.equals(task.getCreatedBy())) {
                throw new AccessDeniedException("DEV can only update their own tasks");
            }
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.estimate() != null) task.setEstimate(request.estimate());
        task.setAssigneeId(request.assigneeId());

        task = taskRepository.save(task);

        eventPublisher.publishUpdated(new TaskUpdatedEvent(
                "TASK_UPDATED", task.getId(), task.getProjectId(), task.getSprintId(),
                task.getTitle(), task.getType(), task.getStatus(), task.getEstimate(),
                task.getAssigneeId(), Instant.now()
        ));

        return toResponse(task);
    }

    @Transactional
    public void updateStatus(UUID id, UpdateTaskStatusRequest request, UUID callerId, String callerRole) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getProjectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, task.getProjectId())) {
                throw new NotProjectMemberException();
            }
        }

        if ("MA".equals(callerRole)) {
            throw new AccessDeniedException("MA cannot update task status");
        }
        
        if (task.getStatus().equals(request.status())) {
            return;
        }

        String fromStatus = task.getStatus();
        task.setStatus(request.status());
        task = taskRepository.save(task);

        eventPublisher.publishStatusChanged(new TaskStatusChangedEvent(
                "TASK_STATUS_CHANGED", task.getId(), task.getProjectId(), task.getSprintId(),
                fromStatus, task.getStatus(), Instant.now()
        ));
    }

    @Transactional
    public void updateSprint(UUID id, UpdateTaskSprintRequest request, UUID callerId, String callerRole) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getProjectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, task.getProjectId())) {
                throw new NotProjectMemberException();
            }
        }

        if ("DEV".equals(callerRole) || "MA".equals(callerRole)) {
            throw new AccessDeniedException("DEV and MA cannot update sprint");
        }

        if (Objects.equals(task.getSprintId(), request.sprintId())) {
            return;
        }

        task.setSprintId(request.sprintId());
        task = taskRepository.save(task);

        eventPublisher.publishUpdated(new TaskUpdatedEvent(
                "TASK_UPDATED", task.getId(), task.getProjectId(), task.getSprintId(),
                task.getTitle(), task.getType(), task.getStatus(), task.getEstimate(),
                task.getAssigneeId(), Instant.now()
        ));
    }

    @Transactional
    public void deleteTask(UUID id, UUID callerId, String callerRole) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getProjectId() != null && !"ADMIN".equals(callerRole)) {
            if (!memberRepository.isMember(callerId, task.getProjectId())) {
                throw new NotProjectMemberException();
            }
        }

        if ("DEV".equals(callerRole) || "MA".equals(callerRole)) {
            throw new AccessDeniedException("DEV and MA cannot delete tasks");
        }

        taskRepository.delete(task);

        eventPublisher.publishDeleted(new TaskDeletedEvent(
                "TASK_DELETED", task.getId(), task.getProjectId(), Instant.now()
        ));
    }

    private void validateTaskType(String type) {
        if (!PROJECT_TASK_TYPES.contains(type) && !OUT_OF_PROJECT_TASK_TYPES.contains(type)) {
            throw new InvalidTaskTypeException("Invalid task type: " + type);
        }
    }

    private void validateTypeProjectConsistency(String type, UUID projectId) {
        if (PROJECT_TASK_TYPES.contains(type) && projectId == null) {
            throw new InvalidTaskTypeException("Project ID is required for task type " + type);
        }
        if (OUT_OF_PROJECT_TASK_TYPES.contains(type) && projectId != null) {
            throw new InvalidTaskTypeException("Project ID must be null for out-of-project task type " + type);
        }
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(), task.getProjectId(), task.getSprintId(),
                task.getTitle(), task.getDescription(), task.getType(),
                task.getPriority(), task.getStatus(), task.getEstimate(),
                task.getAssigneeId(), task.getCreatedBy(), task.getCreatedAt(), task.getUpdatedAt()
        );
    }
}
