package com.mongodb.kitchensink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoIndexConfig {

    @Bean
    public boolean createMongoIndexes(MongoTemplate mongoTemplate, MongoMappingContext mappingContext) {
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        mappingContext.getPersistentEntities().forEach(entity -> {
            IndexOperations indexOps = mongoTemplate.indexOps(entity.getType());
            resolver.resolveIndexFor(entity.getType()).forEach(indexOps::ensureIndex);
        });

        return true;
    }
}