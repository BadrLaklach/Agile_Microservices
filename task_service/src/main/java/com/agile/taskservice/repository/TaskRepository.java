package com.agile.taskservice.repository;

import com.agile.taskservice.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
        SELECT t FROM Task t
        WHERE (:filterProjectNull = true  AND t.projectId IS NULL)
           OR (:filterProjectNull = false AND (:projectId IS NULL OR t.projectId = :projectId))
        AND  (:filterSprintNull  = true  AND t.sprintId  IS NULL
           OR :filterSprintNull  = false AND (:sprintId  IS NULL OR t.sprintId  = :sprintId))
        AND  (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
        AND  (:status     IS NULL OR t.status     = :status)
        AND  (:type       IS NULL OR t.type       = :type)
        AND  (:priority   IS NULL OR t.priority   = :priority)
        """)
    Page<Task> findAllFiltered(
        @Param("projectId")         UUID    projectId,
        @Param("filterProjectNull") boolean filterProjectNull,
        @Param("sprintId")          UUID    sprintId,
        @Param("filterSprintNull")  boolean filterSprintNull,
        @Param("assigneeId")        UUID    assigneeId,
        @Param("status")            String  status,
        @Param("type")              String  type,
        @Param("priority")          String  priority,
        Pageable pageable
    );

    @Query("""
        SELECT t FROM Task t
        WHERE (t.projectId IN :projectIds OR t.projectId IS NULL)
        AND  (:filterSprintNull = true  AND t.sprintId IS NULL
           OR :filterSprintNull = false AND (:sprintId IS NULL OR t.sprintId = :sprintId))
        AND  (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
        AND  (:status     IS NULL OR t.status     = :status)
        AND  (:type       IS NULL OR t.type       = :type)
        AND  (:priority   IS NULL OR t.priority   = :priority)
        """)
    Page<Task> findAllFilteredWithProjectIds(
        @Param("projectIds")        List<UUID> projectIds,
        @Param("sprintId")          UUID       sprintId,
        @Param("filterSprintNull")  boolean    filterSprintNull,
        @Param("assigneeId")        UUID       assigneeId,
        @Param("status")            String     status,
        @Param("type")              String     type,
        @Param("priority")          String     priority,
        Pageable pageable
    );

    @Query("""
        SELECT t.status, COUNT(t)
        FROM Task t
        WHERE (:projectId IS NULL OR t.projectId = :projectId)
          AND (:sprintId  IS NULL OR t.sprintId  = :sprintId)
        GROUP BY t.status
        """)
    List<Object[]> countByStatus(
        @Param("projectId") UUID projectId,
        @Param("sprintId")  UUID sprintId
    );

    @Query("""
        SELECT t.type, COUNT(t)
        FROM Task t
        WHERE (:projectId IS NULL OR t.projectId = :projectId)
          AND (:sprintId  IS NULL OR t.sprintId  = :sprintId)
        GROUP BY t.type
        """)
    List<Object[]> countByType(
        @Param("projectId") UUID projectId,
        @Param("sprintId")  UUID sprintId
    );
}
