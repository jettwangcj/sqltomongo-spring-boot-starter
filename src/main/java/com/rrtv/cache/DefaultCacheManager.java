package com.rrtv.cache;

import com.rrtv.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname DefaultCacheManager
 * @Description
 * @Date 2022/8/24 22:02
 * @Created by wangchangjiu
 */
public class DefaultCacheManager implements CacheManager {

    private Map<String, Set<String>> tableCacheIndexMap = new ConcurrentHashMap<>();
    /**
     *  维护 缓存
     */
    private Cache cache;

    @Override
    public Cache createCache(String fullClassName) {
        if(cache != null){
            return cache;
        }
        if(StringUtils.isNotBlank(fullClassName)){
            try {
                Class<?> aClass = Class.forName(fullClassName);
                this.cache = Cache.class.cast(aClass.newInstance());
            } catch (ClassNotFoundException | IllegalAccessException|InstantiationException e) {
                throw new RuntimeException("create cache " + fullClassName + " fail ：" +  e.getMessage() );
            }
        } else {
            this.cache = new ConcurrentHashMapCache();
        }
        return this.cache;
    }

    @Override
    public Set<String> getTableCacheIndex(String table) {
        return tableCacheIndexMap.get(table.toLowerCase());
    }

    @Override
    public void clearTableCacheIndex(String table) {
        Set<String> tableCacheIndex = this.getTableCacheIndex(table.toUpperCase());
        if(!CollectionUtils.isEmpty(tableCacheIndex)){
            tableCacheIndex.stream().parallel().forEach(item -> this.cache.removeObject(item));
        }
    }

    @Override
    public void addTableCacheIndex(String table, String cacheKey) {
        table = table.toUpperCase();
        Set<String> tableIndex;
        synchronized (table) {
            if(tableCacheIndexMap.containsKey(table)){
                tableIndex = tableCacheIndexMap.get(table);
            } else {
                tableIndex = new HashSet<>();
            }
            tableIndex.add(cacheKey);
            tableCacheIndexMap.put(table, tableIndex);
        }
    }
}
