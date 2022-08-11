package com.rrtv.orm;

import com.rrtv.binding.MapperAnnotationBuilder;
import com.rrtv.binding.MapperProxyFactory;
import com.rrtv.exception.BindingException;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Configuration
 * @Description 解析 配置
 * @Date 2022/8/9 17:30
 * @Created by wangchangjiu
 */
public class Configuration {

    // Mapper 代理
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    // 接口SQL映射
    private Map<String, XNode> mapperElement;


    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperProxyFactory.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperProxyFactory.");
            }
            boolean loadCompleted = false;
            try {
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // 解析 Mapper 注解
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(this, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }


    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }
}
