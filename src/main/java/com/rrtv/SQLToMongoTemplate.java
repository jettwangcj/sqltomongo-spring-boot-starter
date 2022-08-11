package com.rrtv;

import com.rrtv.executor.Executor;
import org.springframework.lang.Nullable;

import java.util.List;

public class SQLToMongoTemplate {

    private Executor executor;

    public SQLToMongoTemplate(Executor executor){
        this.executor = executor;
    }


    public <T> T selectOne(String sql, Class<T> returnType, @Nullable Object... parameters) {
        return executor.selectOne(sql, returnType, parameters);
     }


    public <T> List<T> selectList(String sql, Class<T> returnType, @Nullable Object... parameters) {
      return executor.selectList(sql, returnType, parameters);
    }


}
