package sahmoudi.agile.project_management.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberId implements Serializable {

    private UUID projectId;
    private UUID userId;
}
