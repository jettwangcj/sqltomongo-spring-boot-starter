package com.rrtv.orm;

import org.springframework.lang.Nullable;

import java.util.List;

public interface SqlSession {

    Object execute(String statement, Class returnType, Class actualType, @Nullable Object... parameters);

    <T> T selectOne(String statement, Class<T> returnType, @Nullable Object... parameters);

    <T> List<T> selectList(String statement, Class<T> returnType, @Nullable Object... parameters);

}
