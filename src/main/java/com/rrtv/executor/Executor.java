package com.rrtv.executor;

import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @Classname Executor
 * @Description
 * @Date 2022/8/9 16:56
 * @Created by wangchangjiu
 */
public interface Executor {

    <T> T selectOne(String statement, Class<T> returnType, @Nullable Object... parameters);

    <T> List<T> selectList(String statement, Class<T> returnType, @Nullable Object... parameters);

}
