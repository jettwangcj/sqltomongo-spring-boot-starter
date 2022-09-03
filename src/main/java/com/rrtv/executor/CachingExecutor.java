package com.rrtv.executor;

import com.rrtv.cache.Cache;
import com.rrtv.cache.CacheManager;
import com.rrtv.orm.Configuration;
import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.PartSQLParserData;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @Classname CacheExecutor
 * @Description 带缓存的 SQL查询执行器  装饰者模式
 * @Date 2022/8/10 18:02
 * @Created by wangchangjiu
 */
public class CachingExecutor implements Executor {

    private final Executor delegate;
    private final Cache cache;
    private final CacheManager cacheManager;

    public CachingExecutor(Executor delegate, Configuration configuration) {
        this.delegate = delegate;
        this.cache = configuration.getCache();
        this.cacheManager = configuration.getCacheManager();
    }

    private <T> Object processSelect(String sql, Class<T> returnType, BiFunction<Class<T>, PartSQLParserData, Object> function,
                                     Object... parameters){
        // 生成缓存Key
        String cacheKey = cache.generateCacheKey(sql, returnType, parameters);
        // 获取缓存
        Object object = cache.getObject(cacheKey);
        if(object != null) {
            return object;
        }
        PartSQLParserData data = delegate.sqlParserData(sql, parameters);
        Object result = function.apply(returnType, data);

        // 设置缓存
        cache.putObject(cacheKey, result);

        // 设置缓存索引
        String majorTable = data.getMajorTable();
        List<LookUpData> joinParser = data.getJoinParser();
        cacheManager.addTableCacheIndex(majorTable, cacheKey);
        if(!CollectionUtils.isEmpty(joinParser)){
            joinParser.stream().forEach(item -> cacheManager.addTableCacheIndex(item.getTable(), cacheKey));
        }
        return result;
    }

    @Override
    public PartSQLParserData sqlParserData(String sql, Object... parameters) {
        return delegate.sqlParserData(sql, parameters);
    }

    @Override
    public <T> T selectOne(String sql, Class<T> returnType, Object... parameters) {
        return (T) processSelect(sql, returnType, (reType, data) -> delegate.selectOne(reType, data), parameters);
    }

    @Override
    public <T> List<T> selectList(String sql, Class<T> returnType, Object... parameters) {
        return (List<T>) processSelect(sql, returnType, (reType, data) -> delegate.selectList(reType, data), parameters);
    }



    @Override
    public <T> List<T> selectList(Class<T> returnType, PartSQLParserData data) {
        return delegate.selectList(returnType, data);
    }
}
