package com.rrtv.configure;

import com.rrtv.orm.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;


public class SqlToMongoMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(SqlToMongoMapperFactoryBean.class);

    private Class<T> mapperInterface;

    private ApplicationContext applicationContext;

    public SqlToMongoMapperFactoryBean() {}

    @Override
    public T getObject() throws Exception {
        // 获取 SqlSession 通过它来执行查询SQL的操作
        SqlSession sqlSession = applicationContext.getBean(SqlSession.class);
        InvocationHandler handler = (proxy, method, args) -> {
            if(logger.isInfoEnabled()){
                logger.info("代理：{} ，执行SQL操作！{}",proxy.getClass(), method.getName());
            }
            // 当 mapper 接口中的方法返回一个 List 时，需要知道 List中的泛型类型
            Class actualType = getActualType(method);
            return sqlSession.execute(mapperInterface.getName() + "." + method.getName()
                    , method.getReturnType(), actualType , args);
        };
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{ mapperInterface }, handler);
    }

    private Class getActualType(Method method){
        Class actualType = null;
        if(method.getReturnType() == List.class) {
            // 返回集合时 获取集合的泛型
            if(!(method.getGenericReturnType() instanceof ParameterizedTypeImpl)){
                if(logger.isErrorEnabled()){
                    logger.error("method.getGenericReturnType() is not instanceof ParameterizedTypeImpl");
                }
                return null;
            }
            Type actualTypeArgument = ParameterizedTypeImpl.class.cast(method.getGenericReturnType()).getActualTypeArguments()[0];
            if(!(actualTypeArgument instanceof Class)){
                if(logger.isErrorEnabled()){
                    logger.error("actualTypeArgument is not instanceof ParameterizedTypeImpl");
                }
                return null;
            }
            actualType = Class.class.cast(actualTypeArgument);
        }
        return actualType;

    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
