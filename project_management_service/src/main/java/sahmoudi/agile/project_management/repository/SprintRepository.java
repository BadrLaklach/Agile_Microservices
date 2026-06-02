package sahmoudi.agile.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sahmoudi.agile.project_management.model.Sprint;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    List<Sprint> findByProjectId(UUID projectId);
    List<Sprint> findByProjectIdAndStatus(UUID projectId, String status);
    Optional<Sprint> findByIdAndProjectId(UUID id, UUID projectId);
    boolean existsByProjectIdAndStatus(UUID projectId, String status);
}
