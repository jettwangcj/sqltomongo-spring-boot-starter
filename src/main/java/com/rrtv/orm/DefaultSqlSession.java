package com.rrtv.orm;

import com.rrtv.executor.Executor;
import com.rrtv.util.SqlCommonUtil;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;


public class DefaultSqlSession implements SqlSession {


    private final Configuration configuration;

    private final Executor executor;

    private Map<String, XNode> mapperElement;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
        this.mapperElement = configuration.getMapperElement();
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Object execute(String statement, Class returnType, Class actualType, Object... parameters) {

        XNode xNode = mapperElement.get(statement);
        if(xNode.getSqlType() == SqlCommonUtil.SqlType.SELECT){
            // 查询
            if(returnType == List.class){
                // 集合
                return selectList(statement, actualType, parameters);
            } else {
                return selectOne(statement, returnType, parameters);
            }
        }


        return null;
    }

    @Override
    public <T> T selectOne(String statement, Class<T> returnType, @Nullable Object... parameters) {
        XNode xNode = mapperElement.get(statement);
        return executor.selectOne(xNode.getSql(), returnType, parameters);
    }


    @Override
    public <T> List<T> selectList(String statement, Class<T> returnType, @Nullable Object... parameters) {
        XNode xNode = mapperElement.get(statement);
        return executor.selectList(xNode.getSql(), returnType, parameters);
    }


}
