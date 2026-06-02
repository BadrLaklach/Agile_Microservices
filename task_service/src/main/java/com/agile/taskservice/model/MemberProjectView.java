package com.agile.taskservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "member_project_view")
@Data
@Builder
public class MemberProjectView {

    @Id
    private String id;          // "{userId}:{projectId}"

    private UUID   userId;
    private UUID   projectId;
    private String role;
    private Instant joinedAt;

    public static String buildId(UUID userId, UUID projectId) {
        return userId.toString() + ":" + projectId.toString();
    }
}
