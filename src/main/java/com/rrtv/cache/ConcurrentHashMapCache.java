package com.rrtv.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname ConcurrentHashMapCache
 * @Description
 * @Date 2022/8/24 21:12
 * @Created by wangchangjiu
 */
public class ConcurrentHashMapCache implements Cache {

    private ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<>();

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public String generateCacheKey(String sql, Class returnType, Object... parameters) {
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
