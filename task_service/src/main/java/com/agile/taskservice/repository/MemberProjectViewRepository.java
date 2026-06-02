package com.agile.taskservice.repository;

import com.agile.taskservice.model.MemberProjectView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberProjectViewRepository extends MongoRepository<MemberProjectView, String> {

    default boolean isMember(UUID userId, UUID projectId) {
        return existsById(MemberProjectView.buildId(userId, projectId));
    }

    List<MemberProjectView> findAllByUserId(UUID userId);
}
