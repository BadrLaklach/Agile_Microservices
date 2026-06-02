package sahmoudi.agile.project_management.config;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        mongoTemplate.indexOps("task_projections")
            .ensureIndex(new Index().on("projectId", Sort.Direction.ASC));
        mongoTemplate.indexOps("task_projections")
            .ensureIndex(new Index().on("sprintId", Sort.Direction.ASC));
        mongoTemplate.indexOps("task_projections")
            .ensureIndex(new Index().on("projectId", Sort.Direction.ASC).on("status", Sort.Direction.ASC));
    }
}
