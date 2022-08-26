package com.rrtv.cache;

/**
 * @Classname Cache
 * @Description
 * @Date 2022/8/24 21:08
 * @Created by wangchangjiu
 */
public interface Cache {

    /**
     *  放缓存
     * @param key
     * @param value
     */
    void putObject(Object key, Object value);

    /**
     *  取缓存
     * @param key
     * @return
     */
    Object getObject(Object key);

    /**
     *  删缓存
     * @param key
     * @return
     */
    Object removeObject(Object key);

    /**
     *  生成缓存Key
     * @param sql
     * @param returnType
     * @param parameters
     * @return
     */
    String generateCacheKey(String sql, Class returnType, Object... parameters);

    /**
     *  清缓存
     */
    void clear();

}
