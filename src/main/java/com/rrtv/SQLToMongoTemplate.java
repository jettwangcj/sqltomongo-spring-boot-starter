package com.rrtv;

import com.rrtv.orm.SqlSession;
import org.springframework.lang.Nullable;

import java.util.List;

public class SQLToMongoTemplate {

    private SqlSession sqlSession;

    public SQLToMongoTemplate(SqlSession sqlSession){
        this.sqlSession = sqlSession;
    }


    public <T> T selectOne(String sql, Class<T> returnType, @Nullable Object... parameters) {
        return sqlSession.selectOne(sql, returnType, parameters);
     }


    public <T> List<T> selectList(String sql, Class<T> returnType, @Nullable Object... parameters) {
      return sqlSession.selectList(sql, returnType, parameters);
    }


}
