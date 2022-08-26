package com.rrtv.executor;

import com.rrtv.cache.Cache;
import com.rrtv.orm.Configuration;
import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.PartSQLParserData;

import java.util.List;

/**
 * @Classname CacheExecutor
 * @Description 带缓存的 SQL查询执行器  装饰者模式
 * @Date 2022/8/10 18:02
 * @Created by wangchangjiu
 */
public class CachingExecutor implements Executor {

    private final Executor delegate;
    private final Configuration configuration;
    private final Cache cache;

    public CachingExecutor(Executor delegate, Configuration configuration) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.cache = configuration.getCache();
    }

    @Override
    public PartSQLParserData sqlParserData(String sql, Object... parameters) {
        return delegate.sqlParserData(sql, parameters);
    }

    @Override
    public <T> T selectOne(String sql, Class<T> returnType, Object... parameters) {
        // 缓存处理
        String cacheKey = cache.generateCacheKey(sql, returnType, parameters);
        Object object = cache.getObject(cacheKey);
        if(object != null) {
            return (T) object;
        }
        PartSQLParserData data = delegate.sqlParserData(sql, parameters);

        String majorTable = data.getMajorTable();
        List<LookUpData> joinParser = data.getJoinParser();

        T result = delegate.selectOne(returnType, data);
        cache.putObject(cacheKey, result);
        return result;
    }

    @Override
    public <T> List<T> selectList(String sql, Class<T> returnType, Object... parameters) {
        // 缓存处理
        Object object = cache.getObject(cache.generateCacheKey(sql, returnType, parameters));
        if(object != null) {
            return (List<T>) object;
        }
        return delegate.selectList(sql, returnType, parameters);
    }
}
