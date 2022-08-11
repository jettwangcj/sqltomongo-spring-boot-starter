package com.rrtv.binding;

import com.rrtv.orm.Configuration;
import com.rrtv.orm.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SqlToMongoMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(SqlToMongoMapperFactoryBean.class);

    private Class<T> mapperInterface;

    private ApplicationContext applicationContext;

    private SqlSession sqlSession;

    public SqlToMongoMapperFactoryBean() {}

    /**
     *  获取代理类 注册到IOC
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        // 获取 SqlSession 通过它来执行查询SQL的操作
       // SqlSession sqlSession = applicationContext.getBean(SqlSession.class);
      /*  return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{ mapperInterface }, new MapperProxy(sqlSession, mapperInterface));*/
      return this.sqlSession.getConfiguration().getMapper(this.mapperInterface, this.sqlSession);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     *  设置 Mapper 类 添加进 Mapper 解析
     * @param mapperInterface
     */
    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.sqlSession = applicationContext.getBean(SqlSession.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Configuration configuration = this.sqlSession.getConfiguration();
        configuration.addMapper(mapperInterface);
    }
}
