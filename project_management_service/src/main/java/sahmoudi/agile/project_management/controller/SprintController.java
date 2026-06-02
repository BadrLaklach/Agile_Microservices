package sahmoudi.agile.project_management.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sahmoudi.agile.project_management.dto.request.*;
import sahmoudi.agile.project_management.dto.response.*;
import sahmoudi.agile.project_management.service.SprintService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateSprintRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(sprintService.createSprint(projectId, request, callerId, callerRole));
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> listSprints(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String status,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.listSprints(projectId, status, callerId, callerRole));
    }

    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> getSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.getSprint(projectId, sprintId, callerId, callerRole));
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody UpdateSprintRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.updateSprint(projectId, sprintId, request, callerId, callerRole));
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        sprintService.deleteSprint(projectId, sprintId, callerId, callerRole);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{sprintId}/start")
    public ResponseEntity<SprintResponse> startSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.startSprint(projectId, sprintId, callerId, callerRole));
    }

    @PatchMapping("/{sprintId}/close")
    public ResponseEntity<SprintResponse> closeSprint(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.closeSprint(projectId, sprintId, callerId, callerRole));
    }

    // --- Sprint Task Assignment ---

    @PostMapping("/{sprintId}/tasks")
    public ResponseEntity<SprintTaskResponse> assignTask(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @Valid @RequestBody AssignTaskRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(sprintService.assignTask(projectId, sprintId, request, callerId, callerRole));
    }

    @DeleteMapping("/{sprintId}/tasks/{taskId}")
    public ResponseEntity<Void> removeTask(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @PathVariable UUID taskId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        sprintService.removeTask(projectId, sprintId, taskId, callerId, callerRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sprintId}/tasks")
    public ResponseEntity<List<TaskProjectionResponse>> listSprintTasks(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(sprintService.listSprintTasks(projectId, sprintId, callerId, callerRole));
    }
}
