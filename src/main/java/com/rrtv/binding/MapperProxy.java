package com.rrtv.binding;


import com.rrtv.orm.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * MapperProxy代理类，用于代理Mapper接口
 */
public class MapperProxy<T> implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(MapperProxy.class);

    private SqlSession sqlSession;
    private Class<T> mapperInterface;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    /**
     * 所有Mapper接口的方法调用都会走到这里
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       /* String mapperInterface = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String statementId = mapperInterface + "." + methodName;
        // 如果根据接口类型+方法名能找到映射的SQL，则执行SQL
        if (sqlSession.getConfiguration().hasStatement(statementId)) {
            return sqlSession.selectOne(statementId, args, object);
        }
        // 否则直接执行被代理对象的原方法
        return method.invoke(proxy, args);*/

        if(logger.isInfoEnabled()){
            logger.info("代理：{} ，执行SQL操作！{}",proxy.getClass(), method.getName());
        }
        // 当 mapper 接口中的方法返回一个 List 时，需要知道 List中的泛型类型
        Class actualType = getActualType(method);
        return sqlSession.execute(mapperInterface.getName() + "." + method.getName()
                , method.getReturnType(), actualType , args);

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
}
