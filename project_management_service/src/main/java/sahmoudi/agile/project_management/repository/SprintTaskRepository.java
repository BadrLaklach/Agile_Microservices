package sahmoudi.agile.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sahmoudi.agile.project_management.model.SprintTask;
import sahmoudi.agile.project_management.model.SprintTaskId;
import java.util.UUID;
import java.util.List;

public interface SprintTaskRepository extends JpaRepository<SprintTask, SprintTaskId> {
    List<SprintTask> findBySprintId(UUID sprintId);
    boolean existsBySprintIdAndTaskId(UUID sprintId, UUID taskId);
}
