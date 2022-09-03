1. 加缓存
2. 更新操作后 通过 观察者模式 清除缓存
2. 装饰者模式 cache 参考 myBatis CachingExecutor


其实在重构之前 我一直在考虑 需要重构什么？哪些点可以改进
需要增加啥需求？ 缓存、基于注解

注解啥解析时期？
xml解析后封装到一个 Configuration 中，注解解析结果如何封装？

缓存如何做到按照配置开启？ 缓存加载那一层？

解析SQL时如何能让使用者扩展？ 模板方法？还是别的？

会使用哪些设计模式？

Mapper代理bean如何更加优雅

最后阅读 Mybatis 源码



### 前言
之前我写过一个工具，就是通过 原生SQL去查Mongo数据，具体可以查看我的这篇博文：
写完这个工具后就没有再管这个了，当时就是为了实现这样的功能，然后就没有考虑代码的优雅性，以及可扩展性。
后来我重新看这个工具时，感觉这个代码乱糟糟的（也许以后看这个版本也是乱糟糟），所以我决定将这个工具重构一下，
最初的想法就是，增加这个工具的扩展性，因为我的这个工具有一些功能是没有支持的，可以交给使用者自己去扩展，
那这样的话，易扩展性就显得比较重要了。

说到扩展性，我觉得常用的就是使用模板方法设计模式，或者回调机制，把不变的部分做成模板，把灵活变动的地方交由用户自己实现，
但是我在分析工具核心代码时，觉得这几种方式都不好(具体后面分析)

### 代码

### 哪些地方需要扩展
那既然要扩展，就要考虑哪些地方留给用户扩展的口子，我这次主要留了三个地方，一个是核心的SQL执行器（新增概念，Executor），第二个是SQL解析器（parser包下面），
第三个是SQL分析器（新增概念，analyzer包下）

### 这次重构变化有哪些

### 模仿Mybatis,新增 Configuration 配置类概念

### 如何增加SQL注解解析功能

### 最核心变化，模仿Mybatis，通过拦截器来扩展本工具的核心模块


```
sqltomongo-spring-boot-starter
└── src
    └── main
        └── java
            ├──com.rrtv
            │  ├── adapter
            │  │   └── MatchExpressionVisitorAdapter.java  ------ 解析过滤匹配的 ExpressionVisitorAdapter
            │  ├── analyzer                                ------ SQL分析器，将SQL解析的元数据封装成 Mongo API 
            │  │   ├── AbstractAnalyzer.java               
            │  │   ├── Analyzer.java                       
            │  │   ├── GroupAnalyzer.java                 
            │  │   ├── HavingAnalyzer.java                 
            │  │   ├── JoinAnalyzer.java                  
            │  │   ├── LimitAnalyzer.java                 
            │  │   ├── MatchAnalyzer.java                  
            │  │   ├── ProjectAnalyzer.java              
            │  │   └── SortAnalyzer.java                   
            │  ├── annotation
            │  │   ├── EnableSqlToMongoMapper.java         ------ 启动类注解
            │  │   ├── Intercepts.java                     ------ 插件注解
            │  │   ├── Select.java                         ------ 查询注解
            │  │   ├── Signature.java                      ------ 插件注解
            │  │   └── SqlToMongoMapper.java               ------ Mapper 接口类注解
            │  ├── binding                                 ------ 绑定，Mapper接口代理注册Bean
            │  │   ├── MapperAnnotationBuilder.java        ------ Mapper注解解析，解析 Select 注解
            │  │   ├── MapperProxy.java                       
            │  │   ├── MapperProxyFactory.java         
            │  │   └── SqlToMongoMapperFactoryBean.java         
            │  ├── cache                                   ------ 缓存相关       
            │  │   ├── Cache.java         
            │  │   ├── CacheManager.java                   ------ 缓存管理器
            │  │   ├── ClearCacheEvent.java                ------ 清除缓存事件
            │  │   ├── ClearCacheListener.java             ------ 清除缓存监听器
            │  │   ├── ConcurrentHashMapCache.java         
            │  │   ├── DefaultCacheManager.java         
            │  │   ├── MongoTemplateProxy.java         
            │  │   └── SaveMongoEventListener.java         ------ Mongo 监听器
            │  ├── common
            │  │   ├── AggregationFunction.java            ------ 聚合函数枚举
            │  │   ├── ConversionFunction.java             ------ 转化函数枚举
            │  │   ├── ParserPartTypeEnum.java        
            │  │   └── MongoParserResult.java              ------ SQL解析后封装Mongo API 结果
            │  ├── configure
            │  │   ├── SqlToMongoAutoConfiguration.java    ------ 自动配置
            │  │   ├── SqlToMongoMapperFactoryBean.java    ------ SqlToMongoMapper 工厂Bean
            │  │   └── SqlToMongoRegistrar.java            ------ Mapper 接口 注册 
            │  ├── exception                               ------ 自定义异常
            │  │   ├── BindingException.java
            │  │   ├── NotSupportFunctionException.java
            │  │   ├── NotSupportSubSelectException.java
            │  │   ├── PluginException.java
            │  │   ├── SqlParameterException.java
            │  │   ├── SqlParserException.java
            │  │   ├── SqlTypeException.java
            │  │   └── TableAssociationException.java
            │  ├── executor                                ------ 具体执行器 
            │  │   ├── CachingExecutor.java
            │  │   ├── DefaultExecutor.java
            │  │   └── Executor.java
            │  ├── orm
            │  │   ├── Configuration.java                 ------ 核心配置类，重点
            │  │   ├── ConfigurationBuilder.java       
            │  │   ├── DefaultSqlSession.java             ------ SqlSession 实现类
            │  │   ├── DomParser.java                     ------ Dom 解析  
            │  │   ├── SqlSession.java
            │  │   ├── SqlSessionBuilder.java
            │  │   └── XNode.java                         ------ xml 解析结果封装 
            │  ├── parser                                 ------ SQL 解析
            │  │   ├── data                               ------ SQL 各个部分解析结果
            │  │   │   ├── GroupData.java               
            │  │   │   ├── LimitData.java
            │  │   │   ├── LookUpData.java
            │  │   │   ├── MatchData.java
            │  │   │   ├── PartSQLParserData.java
            │  │   │   ├── PartSQLParserResult.java
            │  │   │   ├── ProjectData.java
            │  │   │   └── SortData.java
            │  │   ├── GroupSQLParser.java               ------ 解析 SQL 分组
            │  │   ├── HavingSQLParser.java              ------ 解析 SQL Having   
            │  │   ├── JoinSQLParser.java                ------ 解析 SQL 表关联
            │  │   ├── LimitSQLParser.java               ------ 解析 SQL Limit
            │  │   ├── PartSQLParser.java                ------ 解析 SQL Limit
            │  │   ├── OrderSQLParser.java               ------ 解析 SQL 排序
            │  │   ├── ProjectSQLParser.java             ------ 解析 SQL 查询字段
            │  │   ├── SelectSQLTypeParser.java          ------ SQL 查询解析器，调用各个解析类解析SQL，并将元数据封装 Mongo 查询API
            │  │   └── WhereSQLParser.java               ------ 解析 SQL where 条件   
            │  ├── plugin                                ------ 插件相关，用于扩展
            │  │   ├── Interceptor.java                  ------ 拦截器接口
            │  │   ├── InterceptorChain.java             ------ 拦截器链，封装所有拦截器
            │  │   ├── InterceptorConfigurer.java        ------ 拦截器配置，用于自定义拦截器
            │  │   ├── InterceptorConfigurerAdapter.java ------ 拦截器配置适配器，默认添加拦截器模板
            │  │   ├── InterceptorTemplate.java          ------ 拦截器模板
            │  │   ├── Invocation.java                  
            │  │   └── Plugin.java                       ------ 插件具体逻辑
            │  ├── util
            │  │   ├── SqlCommonUtil.java                ------  SQL 公共 util
            │  │   ├── SqlParameterSetterUtil.java       ------  SQL 设置参数 util 
            │  │   ├── SqlSupportedSyntaxCheckUtil.java  ------  SQL 支持语法检查 util 
            │  │   └── StringUtils.java                
            │  └── SQLToMongoTemplate.java               ------  用于Mongo 查询的 bean，使用者直接注入该 Bean
            └── resources
                └── META-INF
                    └── spring.factories
```
