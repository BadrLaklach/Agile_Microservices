package sahmoudi.agile.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sahmoudi.agile.project_management.model.ProjectMember;
import sahmoudi.agile.project_management.model.ProjectMemberId;
import java.util.UUID;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProjectId(UUID projectId);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    List<ProjectMember> findByUserId(UUID userId);
}
