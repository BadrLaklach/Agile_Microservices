package sahmoudi.agile.project_management.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sahmoudi.agile.project_management.model.TaskProjection;
import java.util.UUID;
import java.util.List;

public interface TaskProjectionRepository extends MongoRepository<TaskProjection, String> {
    List<TaskProjection> findByProjectId(String projectId);
    List<TaskProjection> findBySprintId(String sprintId);
    List<TaskProjection> findByProjectIdAndStatus(String projectId, String status);
    List<TaskProjection> findBySprintIdAndStatus(String sprintId, String status);

    List<TaskProjection> findAllBySprintIdAndAssigneeId(String sprintId, String assigneeId);

    @Query(value = "{ 'sprintId': ?0, 'assigneeId': { $ne: null } }", count = true)
    long countDistinctAssigneesBySprintId(String sprintId);
}
