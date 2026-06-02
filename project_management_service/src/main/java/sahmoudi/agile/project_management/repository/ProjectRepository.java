package sahmoudi.agile.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sahmoudi.agile.project_management.model.Project;
import java.util.UUID;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByCreatedBy(UUID createdBy);
}
