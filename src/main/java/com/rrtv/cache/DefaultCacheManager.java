package com.rrtv.cache;

import java.util.List;
import java.util.Map;

/**
 * @Classname DefaultCacheManager
 * @Description
 * @Date 2022/8/24 22:02
 * @Created by wangchangjiu
 */
public class DefaultCacheManager implements CacheManager {

    @Override
    public Cache createCache(String fullClassName) {
        return null;
    }

    @Override
    public Map<String, List<String>> getTableCacheIndex(String table) {
        return null;
    }

    @Override
    public void addTableCacheIndex(String table, String cacheKey) {

    }
}
