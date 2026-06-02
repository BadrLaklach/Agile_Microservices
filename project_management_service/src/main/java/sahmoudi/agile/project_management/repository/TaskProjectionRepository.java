package sahmoudi.agile.project_management.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sahmoudi.agile.project_management.model.TaskProjection;
import java.util.List;

public interface TaskProjectionRepository extends MongoRepository<TaskProjection, String> {
    List<TaskProjection> findByProjectId(String projectId);
    List<TaskProjection> findBySprintId(String sprintId);
    List<TaskProjection> findByProjectIdAndStatus(String projectId, String status);
    List<TaskProjection> findBySprintIdAndStatus(String sprintId, String status);
}
