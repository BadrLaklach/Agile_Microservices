package sahmoudi.agile.project_management.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SprintTaskId implements Serializable {

    private UUID sprintId;
    private UUID taskId;
}
