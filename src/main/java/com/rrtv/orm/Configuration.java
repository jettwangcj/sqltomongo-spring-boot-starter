package com.rrtv.orm;

import com.rrtv.binding.MapperAnnotationBuilder;
import com.rrtv.binding.MapperProxyFactory;
import com.rrtv.exception.BindingException;
import com.rrtv.executor.CachingExecutor;
import com.rrtv.executor.DefaultExecutor;
import com.rrtv.executor.Executor;
import com.rrtv.parser.*;
import com.rrtv.plugin.Interceptor;
import com.rrtv.plugin.InterceptorChain;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Configuration
 * @Description 解析 配置
 * @Date 2022/8/9 17:30
 * @Created by wangchangjiu
 */
public class Configuration {

    /**
     *  缓存标志
     */
    private final boolean cacheEnabled = false;

    /**
     *  Mapper 代理
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     *  接口SQL映射
     */
    private Map<String, XNode> mapperElement;

    /**
     *  拦截器链条
     */
    private final InterceptorChain interceptorChain = new InterceptorChain();

    /**
     *  创建执行器 缓存执行器 和 默认执行器
     *  并对执行器增加拦截器处理
     * @param mongoTemplate
     * @param selectSQLTypeParser
     * @return
     */
    public Executor newExecutor(MongoTemplate mongoTemplate, SelectSQLTypeParser selectSQLTypeParser) {
        Executor executor;
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        } else {
            executor = new DefaultExecutor(mongoTemplate, selectSQLTypeParser);
        }
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }

    /**
     *  Join 解析器 通过责任链包装，扩展可以在拦截器中做
     * @return
     */
    public JoinSQLParser newJoinSQLParser(){
        JoinSQLParser joinSQLParser = new JoinSQLParser();
        joinSQLParser = (JoinSQLParser) interceptorChain.pluginAll(joinSQLParser);
       return joinSQLParser;
    }

    /**
     *  分组SQL解析器 通过责任链包装，扩展可以在拦截器中做
     * @return
     */
    public GroupSQLParser newGroupSQLParser(){
        GroupSQLParser groupSQLParser = new GroupSQLParser();
        groupSQLParser = (GroupSQLParser) interceptorChain.pluginAll(groupSQLParser);
        return groupSQLParser;
    }

    /**
     *  Having SQL解析器 通过责任链包装，扩展可以在拦截器中做
     * @return
     */
    public HavingSQLParser newHavingSQLParser(){
        HavingSQLParser havingSQLParser = new HavingSQLParser();
        havingSQLParser = (HavingSQLParser)interceptorChain.pluginAll(havingSQLParser);
        return havingSQLParser;
    }

    public ProjectSQLParser newProjectSQLParser(){
        return null;
    }

    public OrderSQLParser newOrderSQLParser(){
        return null;
    }

    public LimitSQLParser newLimitSQLParser(){
        return null;
    }

    /**
     *  添加拦截器
     * @param interceptor
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

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
