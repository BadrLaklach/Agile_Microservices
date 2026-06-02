package sahmoudi.agile.project_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sahmoudi.agile.project_management.dto.response.*;
import sahmoudi.agile.project_management.exception.*;
import sahmoudi.agile.project_management.model.Sprint;
import sahmoudi.agile.project_management.model.TaskProjection;
import sahmoudi.agile.project_management.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskProjectionRepository taskProjectionRepository;

    private void requireMembership(UUID projectId, UUID callerId, String callerRole) {
        if ("ADMIN".equals(callerRole)) return;
        if (!memberRepository.existsByProjectIdAndUserId(projectId, callerId)) {
            throw new AccessDeniedException();
        }
    }

    public BurndownResponse getBurndown(UUID projectId, UUID sprintId, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        Sprint sprint = sprintRepository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow(() -> new SprintNotFoundException(sprintId));

        List<TaskProjection> tasks = taskProjectionRepository.findBySprintId(sprintId.toString());

        int totalEstimate = tasks.stream().mapToInt(t -> t.getEstimate() != null ? t.getEstimate() : 0).sum();
        int completedEstimate = tasks.stream()
            .filter(t -> "DONE".equals(t.getStatus()))
            .mapToInt(t -> t.getEstimate() != null ? t.getEstimate() : 0).sum();
        int remainingEstimate = totalEstimate - completedEstimate;

        long sprintDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate());
        double idealDaily = sprintDays > 0 ? (double) totalEstimate / sprintDays : 0;

        List<BurndownPoint> idealBurndown = new ArrayList<>();
        for (long i = 0; i <= sprintDays; i++) {
            LocalDate date = sprint.getStartDate().plusDays(i);
            double ideal = totalEstimate - (i * idealDaily);
            idealBurndown.add(new BurndownPoint(date, Math.max(0, ideal)));
        }

        return new BurndownResponse(
            sprint.getId(), sprint.getName(),
            sprint.getStartDate(), sprint.getEndDate(),
            totalEstimate, remainingEstimate, completedEstimate,
            idealBurndown
        );
    }

    public VelocityResponse getVelocity(UUID projectId, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        List<Sprint> completedSprints = sprintRepository.findByProjectIdAndStatus(projectId, "COMPLETED");

        List<SprintVelocity> sprintVelocities = completedSprints.stream().map(sprint -> {
            List<TaskProjection> doneTasks = taskProjectionRepository
                .findBySprintIdAndStatus(sprint.getId().toString(), "DONE");
            int velocity = doneTasks.stream()
                .mapToInt(t -> t.getEstimate() != null ? t.getEstimate() : 0).sum();
            return new SprintVelocity(sprint.getId(), sprint.getName(), velocity);
        }).toList();

        double avgVelocity = sprintVelocities.isEmpty() ? 0 :
            sprintVelocities.stream().mapToInt(SprintVelocity::velocity).average().orElse(0);

        return new VelocityResponse(projectId, avgVelocity, sprintVelocities);
    }
}
