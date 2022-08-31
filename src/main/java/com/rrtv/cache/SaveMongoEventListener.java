package com.rrtv.cache;

import com.rrtv.orm.Configuration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * @Classname SaveMongoEventListener
 * @Description
 * @Date 2022/8/31 16:33
 * @Created by wangchangjiu
 */
public class SaveMongoEventListener extends AbstractMongoEventListener<Object> implements ApplicationEventPublisherAware {

    private final Configuration configuration;

    private ApplicationEventPublisher applicationEventPublisher;

    public SaveMongoEventListener(Configuration configuration){
        this.configuration = configuration;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if(source != null && configuration.isCacheEnabled()) {
            // 发布清除缓存事件

            source.getClass().getSimpleName();

            applicationEventPublisher.publishEvent(new ClearCacheEvent(this, ""));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
