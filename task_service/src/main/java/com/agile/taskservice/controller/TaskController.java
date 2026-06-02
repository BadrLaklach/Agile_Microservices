package com.agile.taskservice.controller;

import com.agile.taskservice.dto.request.CreateTaskRequest;
import com.agile.taskservice.dto.request.UpdateTaskRequest;
import com.agile.taskservice.dto.request.UpdateTaskSprintRequest;
import com.agile.taskservice.dto.request.UpdateTaskStatusRequest;
import com.agile.taskservice.dto.response.PagedTaskResponse;
import com.agile.taskservice.dto.response.TaskResponse;
import com.agile.taskservice.dto.response.TaskSummaryResponse;
import com.agile.taskservice.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        TaskResponse response = taskService.createTask(request, callerId, callerRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        TaskResponse response = taskService.getTask(id, callerId, callerRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedTaskResponse> listTasks(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String sprintId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        PagedTaskResponse response = taskService.listTasks(
                projectId, sprintId, assigneeId, status, type, priority, page, size, callerId, callerRole
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TaskSummaryResponse> getSummary(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        TaskSummaryResponse response = taskService.getSummary(projectId, sprintId, callerId, callerRole);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        TaskResponse response = taskService.updateTask(id, request, callerId, callerRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        taskService.updateStatus(id, request, callerId, callerRole);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/sprint")
    public ResponseEntity<Void> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskSprintRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        taskService.updateSprint(id, request, callerId, callerRole);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole
    ) {
        taskService.deleteTask(id, callerId, callerRole);
        return ResponseEntity.noContent().build();
    }
}
