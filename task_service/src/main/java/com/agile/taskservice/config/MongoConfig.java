package com.agile.taskservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig {
    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        mongoTemplate.indexOps("member_project_view")
                .ensureIndex(new Index().on("userId", Sort.Direction.ASC));
        mongoTemplate.indexOps("member_project_view")
                .ensureIndex(new Index().on("projectId", Sort.Direction.ASC));
    }
}
