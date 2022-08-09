package com.rrtv.executor;

import java.util.List;

/**
 * @Classname DefaultExecutor
 * @Description
 * @Date 2022/8/9 16:57
 * @Created by wangchangjiu
 */
public class DefaultExecutor implements Executor{

    @Override
    public <T> T selectOne(String statement, Class<T> returnType, Object... parameters) {
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Class<T> returnType, Object... parameters) {
        return null;
    }
}
