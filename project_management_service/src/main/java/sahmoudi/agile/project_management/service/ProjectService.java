package sahmoudi.agile.project_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sahmoudi.agile.project_management.client.NotificationServiceClient;
import sahmoudi.agile.project_management.client.UserServiceClient;
import sahmoudi.agile.project_management.dto.request.*;
import sahmoudi.agile.project_management.dto.response.*;
import sahmoudi.agile.project_management.exception.*;
import sahmoudi.agile.project_management.model.Project;
import sahmoudi.agile.project_management.model.ProjectMember;
import sahmoudi.agile.project_management.repository.ProjectMemberRepository;
import sahmoudi.agile.project_management.repository.ProjectRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;

    // Helper methods for authorization
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
    public ProjectResponse createProject(CreateProjectRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO");

        Project project = Project.builder()
            .name(request.name())
            .description(request.description())
            .methodology(request.methodology())
            .status("ACTIVE")
            .startDate(request.startDate())
            .endDate(request.endDate())
            .createdBy(callerId)
            .build();
        project = projectRepository.save(project);

        // Auto-add creator as member with their role
        ProjectMember member = ProjectMember.builder()
            .projectId(project.getId())
            .userId(callerId)
            .role(callerRole)
            .build();
        memberRepository.save(member);

        return toResponse(project);
    }

    public List<ProjectResponse> listProjects(UUID callerId, String callerRole) {
        List<Project> projects;
        if ("ADMIN".equals(callerRole)) {
            projects = projectRepository.findAll();
        } else {
            List<UUID> projectIds = memberRepository.findByUserId(callerId)
                .stream().map(ProjectMember::getProjectId).toList();
            projects = projectRepository.findAllById(projectIds);
        }
        return projects.stream().map(this::toResponse).toList();
    }

    public ProjectResponse getProject(UUID projectId, UUID callerId, String callerRole) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO");

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        if (request.name() != null) project.setName(request.name());
        if (request.description() != null) project.setDescription(request.description());
        if (request.methodology() != null) project.setMethodology(request.methodology());
        if (request.startDate() != null) project.setStartDate(request.startDate());
        if (request.endDate() != null) project.setEndDate(request.endDate());

        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse archiveProject(UUID projectId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO");

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        project.setStatus("ARCHIVED");
        project = projectRepository.save(project);
        return toResponse(project);
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(
            p.getId(), p.getName(), p.getDescription(), p.getMethodology(),
            p.getStatus(), p.getStartDate(), p.getEndDate(),
            p.getCreatedBy(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
