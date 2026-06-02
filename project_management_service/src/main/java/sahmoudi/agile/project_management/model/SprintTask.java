package sahmoudi.agile.project_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sprint_tasks", schema = "pm_schema")
@IdClass(SprintTaskId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintTask {

    @Id
    @Column(name = "sprint_id")
    private UUID sprintId;

    @Id
    @Column(name = "task_id")
    private UUID taskId;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt;
}
