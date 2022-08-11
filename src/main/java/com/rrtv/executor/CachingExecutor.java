package com.rrtv.executor;

import java.util.List;

/**
 * @Classname CacheExecutor
 * @Description 带缓存的 SQL查询执行器  装饰者模式
 * @Date 2022/8/10 18:02
 * @Created by wangchangjiu
 */
public class CachingExecutor implements Executor {

    private final Executor delegate;

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> T selectOne(String sql, Class<T> returnType, Object... parameters) {
        // 缓存处理

        return delegate.selectOne(sql, returnType, parameters);
    }

    @Override
    public <T> List<T> selectList(String sql, Class<T> returnType, Object... parameters) {
        // 缓存处理

        return delegate.selectList(sql, returnType, parameters);
    }
}
