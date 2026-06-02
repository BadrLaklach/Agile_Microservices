package sahmoudi.agile.project_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "task_projections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProjection {

    @Id
    private String id;

    private String projectId;

    private String sprintId;

    private String title;

    private String type;

    private String status;

    private Integer estimate;

    private String assigneeId;

    private Instant createdAt;

    private Instant updatedAt;
}
