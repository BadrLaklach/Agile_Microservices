package sahmoudi.agile.project_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sahmoudi.agile.project_management.dto.response.BurndownResponse;
import sahmoudi.agile.project_management.dto.response.VelocityResponse;
import sahmoudi.agile.project_management.service.MetricsService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/sprints/{sprintId}/metrics/burndown")
    public ResponseEntity<BurndownResponse> getBurndown(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(metricsService.getBurndown(projectId, sprintId, callerId, callerRole));
    }

    @GetMapping("/metrics/velocity")
    public ResponseEntity<VelocityResponse> getVelocity(
            @PathVariable UUID projectId,
            @RequestHeader("X-User-Id") UUID callerId,
            @RequestHeader("X-User-Role") String callerRole) {
        return ResponseEntity.ok(metricsService.getVelocity(projectId, callerId, callerRole));
    }
}
