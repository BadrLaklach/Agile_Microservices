package sahmoudi.agile.project_management.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sahmoudi.agile.project_management.dto.request.*;
import sahmoudi.agile.project_management.dto.response.*;
import sahmoudi.agile.project_management.service.MemberService;
import sahmoudi.agile.project_management.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectService.createProject(request, callerId, callerRole));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(projectService.listProjects(callerId, callerRole));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(projectService.getProject(projectId, callerId, callerRole));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request, callerId, callerRole));
    }

    @PatchMapping("/{projectId}/archive")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(projectService.archiveProject(projectId, callerId, callerRole));
    }

    // --- Member endpoints nested under project ---

    @PostMapping("/{projectId}/members")
    public ResponseEntity<MemberResponse> inviteMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody InviteMemberRequest request,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(memberService.inviteMember(projectId, request, callerId, callerRole));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<MemberResponse>> listMembers(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(memberService.listMembers(projectId, callerId, callerRole));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        memberService.removeMember(projectId, userId, callerId, callerRole);
        return ResponseEntity.noContent().build();
    }
}
