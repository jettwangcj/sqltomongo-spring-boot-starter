package com.rrtv.cache;

import org.springframework.context.ApplicationEvent;

/**
 * @Classname ClearCacheEvent
 * @Description
 * @Date 2022/8/9 16:44
 * @Created by wangchangjiu
 */
public class ClearCacheEvent extends ApplicationEvent {

    public ClearCacheEvent(Object source) {
        super(source);
    }

}
