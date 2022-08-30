package com.rrtv.executor;

import com.rrtv.parser.data.PartSQLParserData;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @Classname Executor
 * @Description
 * @Date 2022/8/9 16:56
 * @Created by wangchangjiu
 */
public interface Executor {

    PartSQLParserData sqlParserData(String sql, Object... parameters);

    default <T> T selectOne(Class<T> returnType, PartSQLParserData data){
        throw new RuntimeException("method not support");
    }

    <T> T selectOne(String sql, Class<T> returnType, @Nullable Object... parameters);

    <T> List<T> selectList(String sql, Class<T> returnType, @Nullable Object... parameters);

    <T> List<T> selectList(Class<T> returnType, PartSQLParserData data);

}
