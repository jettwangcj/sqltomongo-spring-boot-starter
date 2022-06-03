package com.rrtv.orm;

import com.rrtv.SQLToMongoTemplate;
import com.rrtv.util.SqlCommonUtil;
import net.sf.jsqlparser.JSQLParserException;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;


public class DefaultSqlSession implements SqlSession {

    private SQLToMongoTemplate sqlToMongoTemplate;

    private Map<String, XNode> mapperElement;

    public DefaultSqlSession(SQLToMongoTemplate sqlToMongoTemplate, Map<String, XNode> mapperElement) {
        this.sqlToMongoTemplate = sqlToMongoTemplate;
        this.mapperElement = mapperElement;
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
        return sqlToMongoTemplate.selectOne(xNode.getSql(), returnType, parameters);
    }


    @Override
    public <T> List<T> selectList(String statement, Class<T> returnType, @Nullable Object... parameters) {
        XNode xNode = mapperElement.get(statement);
        return sqlToMongoTemplate.selectList(xNode.getSql(), returnType, parameters);
    }


}
