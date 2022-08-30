package com.rrtv.cache;

import com.mongodb.client.result.UpdateResult;
import com.rrtv.orm.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.lang.Nullable;

@Slf4j
public class MongoTemplateProxy extends MongoTemplate implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    private final Configuration configuration;

    public MongoTemplateProxy(MongoDatabaseFactory mongoDbFactory, Configuration configuration) {
        super(mongoDbFactory);
        this.configuration = configuration;
    }

    @Override
    protected UpdateResult doUpdate(String collectionName, Query query, UpdateDefinition update,
                                    @Nullable Class<?> entityClass, boolean upsert, boolean multi)  {
        UpdateResult updateResult = super.doUpdate(collectionName, query, update, entityClass, upsert, multi);
        if(configuration.isCacheEnabled()){
            // 发布清除缓存事件
            applicationEventPublisher.publishEvent(new ClearCacheEvent(this, collectionName));
        }
        return updateResult;
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
