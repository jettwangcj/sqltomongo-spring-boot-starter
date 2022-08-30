package com.rrtv.cache;

import java.util.Set;

/**
 * @Classname CacheManager
 * @Description
 * @Date 2022/8/24 22:01
 * @Created by wangchangjiu
 */
public interface CacheManager {

    /**
     *  创建缓存
     * @param fullClassName
     * @return
     */
    Cache createCache(String fullClassName);


    /**
     *  获取表索引 用于更新数据时 找到缓存索引  清除脏缓存
     * @param table
     * @return
     */
    Set<String> getTableCacheIndex(String table);

    /**
     *  清除
     * @param table
     */
    void clearTableCacheIndex(String table);

    /**
     *  添加缓存索引
     * @param table
     * @param cacheKey
     */
    void addTableCacheIndex(String table, String cacheKey);


}
