package com.rrtv.orm;

import com.rrtv.analyzer.*;
import com.rrtv.binding.MapperAnnotationBuilder;
import com.rrtv.binding.MapperProxyFactory;
import com.rrtv.cache.Cache;
import com.rrtv.cache.CacheManager;
import com.rrtv.common.ParserPartTypeEnum;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname Configuration
 * @Description 解析 配置
 * @Date 2022/8/9 17:30
 * @Created by wangchangjiu
 */
public class Configuration {

    /**
     * 缓存标志
     */
    private boolean cacheEnabled = false;

    /**
     *  缓存管理器
     */
    private CacheManager cacheManager;


    /**
     *  缓存
     */
    private Cache cache;

    /**
     * Mapper 代理
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * 接口SQL映射
     */
    private Map<String, XNode> mapperElement;

    /**
     * 拦截器链条
     */
    private static final InterceptorChain interceptorChain = new InterceptorChain();

    /**
     *  SQL 分析器
     */
    private static volatile Analyzer analyzer;

    /**
     *  SQL 解析器缓存
     */
    private static Map<ParserPartTypeEnum, PartSQLParser> partSQLParserCache = new ConcurrentHashMap<>();


    /**
     *  创建 SQL 分析器责任链 （被拦截器包裹的）
     * @return
     */
    public static Analyzer getAnalyzerInstance() {
        if (analyzer == null) { //双重检测机制
            synchronized (Analyzer.class) { //同步锁
                if (analyzer == null) { //双重检测机制
                    analyzer = new AbstractAnalyzer.Builder()
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new JoinAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new MatchAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new GroupAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new HavingAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new SortAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new LimitAnalyzer()))
                            .addAnalyzer((Analyzer) interceptorChain.pluginAll(new ProjectAnalyzer()))
                            .build();
                }
            }
        }
        return analyzer;
    }

    /**
     *  优先缓存获取实例
     * @param typeEnum
     * @return
     */
    public PartSQLParser getPartSQLParserInstance(ParserPartTypeEnum typeEnum){
        if(partSQLParserCache.containsKey(typeEnum)){
            return partSQLParserCache.get(typeEnum);
        }
        PartSQLParser partSQLParser = newPartSQLParser(typeEnum);
        partSQLParserCache.put(typeEnum, partSQLParser);
        return partSQLParser;
    }

    /**
     *  创建 SQL 解析器
     * @param typeEnum
     * @return
     */
    public PartSQLParser newPartSQLParser(ParserPartTypeEnum typeEnum) {
        PartSQLParser partSQLParser = null;
        if (typeEnum == ParserPartTypeEnum.GROUP) {
            partSQLParser = new GroupSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.HAVING) {
            partSQLParser = new HavingSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.LIMIT) {
            partSQLParser = new LimitSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.PROJECT) {
            partSQLParser = new ProjectSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.ORDER) {
            partSQLParser = new OrderSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.JOIN) {
            partSQLParser = new JoinSQLParser();
        } else if (typeEnum == ParserPartTypeEnum.WHERE) {
            partSQLParser = new WhereSQLParser();
        }
        partSQLParser = (PartSQLParser) interceptorChain.pluginAll(partSQLParser);
        return partSQLParser;
    }

    /**
     * 创建执行器 缓存执行器 和 默认执行器
     * 并对执行器增加拦截器处理
     *
     * @param mongoTemplate
     * @param selectSQLTypeParser
     * @return
     */
    public Executor newExecutor(MongoTemplate mongoTemplate, SelectSQLTypeParser selectSQLTypeParser) {
        Executor executor = new DefaultExecutor(mongoTemplate, selectSQLTypeParser);
        if (cacheEnabled) {
            executor = new CachingExecutor(executor, this);
        }
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }


    /**
     * 添加拦截器
     *
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

    /**
     *  判断是否有 Mapper
     * @param type
     * @param <T>
     * @return
     */
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    /**
     *  添加 Mapper
     * @param type
     * @param <T>
     */
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

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void addMapperElement(String key, XNode xNode) {
        this.mapperElement.put(key, xNode);
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public Cache getCache() {
        return cache;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


}
