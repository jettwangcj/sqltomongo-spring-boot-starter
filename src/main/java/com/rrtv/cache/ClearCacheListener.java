package com.rrtv.cache;

import com.rrtv.orm.Configuration;
import org.springframework.context.ApplicationListener;

/**
 * @Classname ClearCacheListener
 * @Description
 * @Date 2022/8/9 16:49
 * @Created by wangchangjiu
 */
public class ClearCacheListener implements ApplicationListener<ClearCacheEvent> {

    private Configuration configuration;

    public ClearCacheListener(Configuration configuration){
        this.configuration = configuration;
    }

    @Override
    public void onApplicationEvent(ClearCacheEvent clearCacheEvent) {
        CacheManager cacheManager = configuration.getCacheManager();
        if(cacheManager != null){
            String collectionName = clearCacheEvent.getCollectionName();
            cacheManager.clearTableCacheIndex(collectionName);
        }
    }
}
