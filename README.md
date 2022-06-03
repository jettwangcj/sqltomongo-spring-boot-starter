# 前言
我先谈谈为啥要搞这个插件，最近我们项目中使用 MongoDb 这款文档数据库来代替关系型数据库，说实话，我之前没咋用过Mongo，所以用的时候临时抱佛脚查API，但我个人觉得 Mongo 的API和 Hibernate 一样不太好用（纯个人感觉，各位大侠别喷）。

所以，既然 Mongo 也是一款数据库，那我能不能用原生 SQL 去查数据呢？  
为此我在网上搜索了一圈也没找到，当然有个 Studio 3T 工具可以用SQL查，但是没有Java的jar包去干这个事情，所以就萌生一种造个工具的想法。
> 后来我发现 MyCat 有通过原生SQL去查Mongo的功能，所以我本打算把MyCat的这部分代码移过来，单独搞个这个的工具。但是看了一下 MyCat 的实现，我还是放弃了，原因主要是以下两点:
> 1. MyCat 使用 JDBC 规范，抽象了对 MongoDB 的访问，所以搞出了一些比如 MongoConnection、MongoStatement、MongoResultRet。不是说这些不好，只是我觉得这样做比较复杂，还需要引入JDBC规范。
>
> 2. MyCat 使用的SQL解析器是 druid，这也不是我的选择，后面会讲到
     > 当然还有最重要一点，我想自己造轮子，哈哈


# 这个工具咋设计呢？
其实设计这个工具思路很简单，就是将 SQL 语句解析出来，比如解析出表、字段、where条件、分组、排序、分页等等，然后将这些元数据封装成 MongoDB API，发起 Mongo 请求查询数据，封装返回结果。

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3dca20052e514a8aabab606d15f57d62~tplv-k3u1fbpfcp-watermark.image?)

由上图可知，SQL转Mongo工具中最关键的就在于第一步和第二步。

## SQL解析器选择
SQL解析是有这种工具的，常见的有 **druid sql parser** 、 **jsqlparser** 、 **fdb-sql-parser**

我这里选择的是 **jsqlparser**。

我看有人做过对比，由于 **fdb-sql-parser** 不支持复杂的SQL，因此，首先排除，都不支持执行SQL语句解析语义。  
其次，我个人感觉 **druid** 的功能过于强大，SQL解析只是它的一部分功能，而我这个工具专注的是SQL解析，所以我选择轻量级的 **jsqlparser**，另外更重要的是我之前就使用过 **jsqlparser**。

## Mongo 客户端选择
常见的 Mongo java client 有这么几种：

1、[MongoDBJava Driver](https://www.mongodb.com/docs/drivers/java/sync/current/#mongodb-java-driver)   
2、[MongoDB Java Reactive Streams](https://www.mongodb.com/docs/drivers/reactive-streams/)  
3、[spring-data-mongodb](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.core)  
4、spring-boot-starter-data-mongodb

这里选择的是 **spring-boot-starter-data-mongodb**， 因为我这个项目本身就是会做成一个 spring boot starter。很容易就接入 **spring-boot-starter-data-mongodb**，另外，这个jar 包里面提供了一个 **MongoTemplate** ，功能非常强大，我这个工具不满足使用的情况下，可以直接使用 MongoTemplate。

# 实操篇

## 支持的功能
支持的核心功能就是将 给定的SQL去查Mongo 数据库，返回查询结果了，这里只支持 SELECT 查询语句，其他的个人感觉使用 Mongo API也不复杂，就不提供了。

所以，这里要重点提一下，我支持哪些 SQL语法了，毕竟SQL还是很复杂了，我并没有实现所有的语法都能
解析，只是能解析使用的比较多的语法

1. 支持单表查询

2. 支持多表关联查询  
   表关联时只支持等值关联，关联时支持函数 ObjectId、string

3. 支持 where 条件过滤

4. 支持 group by 分组

5. 支持 having 过滤

6. 支持 order by 排序

7. 支持 limit 分页

8. 支持部分聚合函数:  
   count、sum、avg、min、max、first、last，主要是Mongo支持这些操作

尤为注意的一点是，**不支持子查询，不支持子查询，不支持子查询**。重要的事情说三遍！


另外再使用上有两种方式，具体后面代码会分析
- 使用 SQLToMongoTemplate ，SQL直接写在 Java 代码里面。

- 类似 Mybatis，提供解析 xml ，提取 SQL 的功能

## 项目整体结构

### 主要类结构图
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1fd4f0197dbe412b96717b130fea9164~tplv-k3u1fbpfcp-watermark.image?)

由上面图可以看到，整个工具可以一分为二

左边实现的是类似Mybatis的查询功能，只要Mapper，Mapper被注解 `SqlToMongoMapper` 修饰，SQL语句写在 XML文件里面。

右边就是将拿到的SQL进行解析，这里有个核心类 `SelectSQLTypeParser` ，这个类会调用其他类去解析SQL各个部分的语句，拿到解析的结果之后再通过 `MongoTemplate` 的聚合函数 `aggregate` 去 Mongo 查询数据。

## 详细设计
### 如何实现类似Mybatis的功能
我们知道 Mybatis 查询操作，在编写dao层时，只需要提供mapper接口与相应的`xxxMapper.xml`，无需实现类，便可以将mapper接口对象交由Spring容器管理。

那这里有两个核心的问题
1.  什么时候为 mapper接口生成代理对象的？
1.  如何将 mapper对象交给Spring管理？

其实这一部分需要熟悉Spring的机制，简单的来说就是，Spring 的Bean在创建以及初始化过程中，暴露了一些口子，可以人为的干预Bean的生成。

在启动类注解上导入一个 `ImportBeanDefinitionRegistrar` 的实现类，通过`registerBeanDefinitions` 方法扫描指定mapper包下面的接口，为这些接口生成代理对象，再通过FactoryBean将这些代理对象交给Spring管理。具体看下面分析

#### 代理类注册Bean的过程
启动类注解：  
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7e3d9a9df0694da6a4400e4cdb164eef~tplv-k3u1fbpfcp-watermark.image?)

这个注解 Import 了 `SqlToMongoRegistrar`，看一下这个类的核心方法
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/354483b7d5ae41ecbef659ad6c497d07~tplv-k3u1fbpfcp-watermark.image?)
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/490f96a5c33a40ec9a95bb2bd16611a7~tplv-k3u1fbpfcp-watermark.image?)
1. 首先创建`ClassPathScanningCandidateComponentProvider` ，它是 spring 的一个内部工具类，可以帮助我们从包路径中获取到所需的 BeanDefinition 集合。
2. 为扫描器添加一个注解过滤器，有 `SqlToMongoMapper` 注解的类/接口才继续处理。
3. 获取到启动器上面注解 `EnableSqlToMongoMapper`，得到扫描 mapper 接口的包名。
4. 遍历包名，通过 `findCandidateComponents`方法，扫描指定的包路径，获取相应的BeanDefinition，扫描后的类可以通过过滤器进行排除。

接下来就是将 Mapper接口的代理对象注册到Spring容器了
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3d4a4a2727b042848a1c50718a38b4e4~tplv-k3u1fbpfcp-watermark.image?)

可以看到有个 `SqlToMongoMapperFactoryBean` ，这个类实现了 `FactoryBean`，`FactoryBean`是一个工厂Bean，可以生成某一个类型Bean实例，那具体生成哪个Bean呢？就是 `definition.addPropertyValue("mapperInterface", className);` 这句代码，将需要被代理的接口类型以属性的方式赋值。


接下来看 `SqlToMongoMapperFactoryBean` 核心代码
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f698ceec2d764821b951b8154cee78c1~tplv-k3u1fbpfcp-watermark.image?)

这个 `getObject` 方法就是返回一个 Bean 的实例，也就是 Mapper 接口的代理，可以看到这个代理持有 `SqlSession` 对象，通过 `SqlSession` 对象去查数据。

到这里，一开始抛出的两个问题就解决了，为 Mapper 接口生成代理对象，以及将代理对象交由 Spring 来管理了。

当然还有第二个问题，XML中SQL语句解析，那啥时候解析的呢？继续往下看

#### Mapper.xml 解析，准备好SQL语句
在项目启动的时候就需要扫描 xml 路径，通过 Dom4j 解析出SQL语句以及对应的接口和方法。
具体是基于 springboot starter的SPI机制，找到spring.factories配置文件，进而加载配置类。

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/82f5cebb96984d4caf53048ade0fc2ed~tplv-k3u1fbpfcp-watermark.image?)

这个配置类里面会创建 `SqlSession` 这个bean，这个 bean 会通过 `SqlSessionBuilder` 来创建

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1efd789bcb4448839afc4ca8a46eb375~tplv-k3u1fbpfcp-watermark.image?)
可以看到创建 `SqlSession` 的时候就读取并解析 XML文件了，`PathMatchingResourcePatternResolver` 是一个资源查找器，可以用来查找类路径下或者文件系统中的资源。然后通过 Dom4j 解析XML文件。

最后通过 parser 方法将解析的信息封装成一个对象，存储到内存中。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/53ec26f5b5744425ae34ff1f2b27c84d~tplv-k3u1fbpfcp-watermark.image?)

到此，已经实现了读取解析 mapper.xml 文件了，并把sql语句相关信息保存在内存中。


### 工程目录结构
```
sqltomongo-spring-boot-starter
└── src
    └── main
        └── java
            ├──com.rrtv
            │  ├── adapter
            │  │   └── MatchExpressionVisitorAdapter.java  ------ 解析过滤匹配的 ExpressionVisitorAdapter
            │  ├── annotation
            │  │   ├── EnableSqlToMongoMapper.java         ------ 启动类注解
            │  │   └── SqlToMongoMapper.java               ------ Mapper 接口类注解
            │  ├── common
            │  │   ├── AggregationFunction.java            ------ 聚合函数枚举
            │  │   ├── ConversionFunction.java             ------ 转化函数枚举
            │  │   └── MongoParserResult.java              ------ SQL解析后封装Mongo API 结果
            │  ├── configure
            │  │   ├── SqlToMongoAutoConfiguration.java    ------ 自动配置
            │  │   ├── SqlToMongoMapperFactoryBean.java    ------ SqlToMongoMapper 工厂Bean
            │  │   └── SqlToMongoRegistrar.java            ------ Mapper 接口 注册 
            │  ├── exception                               ------ 自定义异常
            │  │   ├── NotSupportFunctionException.java
            │  │   ├── NotSupportSubSelectException.java
            │  │   ├── SqlParameterException.java
            │  │   ├── SqlParserException.java
            │  │   ├── SqlTypeException.java
            │  │   └── TableAssociationException.java
            │  ├── orm
            │  │   ├── DefaultSqlSession.java             ------ SqlSession 实现类
            │  │   ├── DomParser.java                     ------ Dom 解析  
            │  │   ├── SqlSession.java
            │  │   ├── SqlSessionBuilder.java
            │  │   └── XNode.java                         ------ xml 解析结果封装 
            │  ├── parser
            │  │   ├── data                               ------ SQL 各个部分解析结果
            │  │   │   ├── GroupData.java               
            │  │   │   ├── LimitData.java
            │  │   │   ├── LookUpData.java
            │  │   │   ├── MatchData.java
            │  │   │   ├── ProjectData.java
            │  │   │   ├── SortData.java
            │  │   ├── GroupSQLParser.java               ------ 解析 SQL 分组
            │  │   ├── HavingSQLParser.java              ------ 解析 SQL Having   
            │  │   ├── JoinSQLParser.java                ------ 解析 SQL 表关联
            │  │   ├── LimitSQLParser.java               ------ 解析 SQL Limit
            │  │   ├── OrderSQLParser.java               ------ 解析 SQL 排序
            │  │   ├── ProjectSQLParser.java             ------ 解析 SQL 查询字段
            │  │   ├── SelectSQLTypeParser.java          ------ SQL 查询解析器，调用各个解析类解析SQL，并将元数据封装 Mongo 查询API
            │  │   └── WhereSQLParser.java               ------ 解析 SQL where 条件   
            │  ├── util
            │  │   ├── SqlCommonUtil.java                ------  SQL 公共 util
            │  │   ├── SqlParameterSetterUtil.java       ------  SQL 设置参数 util 
            │  │   └── SqlSupportedSyntaxCheckUtil.java  ------  SQL 支持语法检查 util 
            │  └── SQLToMongoTemplate.java               ------  用于Mongo 查询的 bean，使用者直接注入该 Bean
            └── resources
                └── META-INF
                    └── spring.factories
```
核心功能就在 parser 包下面，也是后面着重分析的一个包，主要是解析 SQL语句并完成 将解析的元数据分装成 Mongo API

### 方便 SQL 转 Mongo 查询的模板工具类 SQLToMongoTemplate
`SQLToMongoTemplate` 就类似于 `JDBCTemplate` 一样，方便用户查询 Mongo数据的 Bean，这个类主要提供了两个方法：`selectOne`、`selectList`，这两个方法实现差不多，以`selectOne` 为例。

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/689410351faa487c84d27def824b029c~tplv-k3u1fbpfcp-watermark.image?)
这个方法最主要的就是 设置SQL参数和解析SQL两个核心方法了。

### 设置SQL参数
由于这个方法比较长，就直接贴源代码了
```
public static String parameterSetter(String sql, @Nullable Object... parameters) {
    if (parameters != null && parameters.length > 0) {
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            if (sql.contains("?")) {
                // 使用 ? 占位符
                if(!isSimpleType(param) && !isCollectionType(param)){
                    throw new SqlParameterException("使用 问号(?) 占位方式设置参数不支持对象类型");
                }
                if (isCollectionType(param)) {
                    // 处理集合
                    Iterator iterator = (Collection.class.cast(param)).iterator();
                    StringBuilder sb = new StringBuilder("(");
                    while (iterator.hasNext()) {
                        Object objParam = iterator.next();
                        if (!isSimpleType(objParam)) {
                            // 复杂类型 报错误
                            throw new SqlParameterException("使用 问号(?) 占位方式设置参数，参数是集合类型时，集合元素只支持基本类型");
                        }
                        sb.append(parameterHandle(objParam)).append(",");
                    }
                    sb.deleteCharAt(sb.lastIndexOf(","));
                    sb.append(")");
                    sql = sql.replaceFirst("\?", sb.toString());
                } else {
                    sql = sql.replaceFirst("\?", parameterHandle(param));
                }
            } else {
                // 使用 :name 方式占位
                if(isSimpleType(param)){
                    // 基本类型
                    throw new SqlParameterException("目号(:)占位方式设置参数不支持基本类型");
                }

                // 复杂对象
                AtomicReference<String> reference = new AtomicReference<>(sql);
                ReflectionUtils.doWithFields(param.getClass(), field -> {
                    field.setAccessible(true);
                    Object paramValue = field.get(param);
                    if(!isSimpleType(paramValue)){
                        // 对象嵌套对象 不支持
                        throw new RuntimeException("");
                    }
                    String setterSql = reference.get().replaceFirst(":" + field.getName(), parameterHandle(paramValue));
                    reference.set(setterSql);
                });
                sql = reference.get();
            }
        }
    }

    // 打印日志
    if(logger.isInfoEnabled()){
        logger.info(String.format("设置参数后的SQL: %s", sql));
    }

    return sql;
}
```
这里参数占位符支持两种，一种是 ? 占位符，另一种是 :xxx 这种占位符形式，但是为了简单起见，做了如下约束：

- **"? " 占位符**适用于多个基本类型参数以及集合类型  
  要注意的是集合类型的泛型是基本类型和String  
  举个例子：

    ```
    List<Integer> ids = new ArrayList<>();
    SqlParameterSetterUtil.parameterSetter("select id from t where name = ? and id in (?) ",  "wangchangjiu", ids);
    ```

- **":xxx"** 占位符适用于参数是对象类型  
  举个例子：
    ```
    User user = new User(100, "wangchangjiu")
    SqlParameterSetterUtil.parameterSetter("select id from t where id = :id and name = :name, user)
    ```
设置参数我实现的比较简单，使用 `String` 的 `replaceFirst` 方法替换占位符，值得注意的是，如果参数是 String 类型需要做 **SQL防注入**。

### 利用SQL 解析器解析 SQL

#### 解析出SQL关联表
解析 SQL 关联表之前，来看看 Mongo 的 aggregate 函数是怎么关联表的。
假设两张表，一张是 grade,另一个是 student。
```
// 以grade为主表，左连接student表
db.getCollection("grade").aggregate([
    {
        $lookup:{
            from:"student",// 连接表
            localField: "_id",// 主表关联字段
            foreignField: "gradeId",// 连接表关联字段
            as: "students"// 外键集合数据
        }
    },
])

```
由 Mongo APi 可以看出来，解析 SQL的Join 关系，至少需要知道被关联表的 表名、主表关联字段、连接表关联字段、以及对应的外键集合数据。

所以定义每个表解析出这样的几个字段：
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f2febc30dd2040c6b508195ac85a7be7~tplv-k3u1fbpfcp-watermark.image?)

上图中出现 `private ConversionFunction function;`字段，这里为了解决 Mongo 表关联时的存在的一个问题，就是如果 两张表使用 “_id” 去关联，而且 “_id" 使用 ObjectId类型，那么关联的时候就需要函数转化，举个例子：
```
from tab1 t1 left join tab2 t2 on t1.id = ObjectId(t2.oid)
```
这里的 t1 使用 id 去关联，t2就需要ObjectId函数去转化，这是因为 t1的id是 ObjectId类型，t2表的 oid 外键是 string类型，所以需要转化。

而 `private String conversionFieldTable;` 这个字段是为了后面投影做准备的，因为要对表的字段做函数转化，需要先查询出这个数据，Mongo中就是投影的操作。

解析SQL,贴出代码
```
public static List<LookUpData> parser(List<Join> joins, String majorTableAlias ) {

    List<LookUpData> lookUpData = new ArrayList<>();

    if(!CollectionUtils.isEmpty(joins)){
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            SqlSupportedSyntaxCheckUtil.checkSubSelect(rightItem);
            if (rightItem instanceof Table) {
                Table table = (Table) (rightItem);

                // 检查 表关联时 只支持 等值匹配并且匹配条件只有一个
                SqlSupportedSyntaxCheckUtil.checkTableAssociationCondition(join.getOnExpressions());

                // 根据 外键关联  所以条件只有一个
                Expression onExpression = join.getOnExpression();

                EqualsTo equalsTo = EqualsTo.class.cast(onExpression);

                Expression leftExpression = equalsTo.getLeftExpression();
                Expression rightExpression = equalsTo.getRightExpression();

                ConversionFunction function = null;
                Column left , right;
                String conversionFieldTable = null;

                if(leftExpression instanceof Function){
                    // 检查支持的函数
                    // 因为 JOIN 函数支持 类型转换函数 检查有多个参数报错或者没有参数报错
                   Function leftFunction = Function.class.cast(leftExpression);

                   SqlSupportedSyntaxCheckUtil.checkTableAssociationSupportedFunction(leftFunction);

                   function = ConversionFunction.parser(leftFunction.getName());
                   left = Column.class.cast(leftFunction.getParameters().getExpressions().get(0));
                   conversionFieldTable = left.getTable().getName();
                } else {
                    left = Column.class.cast(leftExpression);
                }

                if(rightExpression instanceof Function){

                    Function rightFunction = Function.class.cast(rightExpression);
                    // 检查支持的函数 因为 JOIN 函数支持 类型转换函数 检查有多个参数报错或者没有参数报错
                    SqlSupportedSyntaxCheckUtil.checkTableAssociationSupportedFunction(rightFunction);

                    function = ConversionFunction.parser(rightFunction.getName());
                    right = Column.class.cast(rightFunction.getParameters().getExpressions().get(0));
                    conversionFieldTable = right.getTable().getName();
                } else {
                    right = Column.class.cast(rightExpression);
                }

                String currTableAlias = table.getAlias() == null ? "" : table.getAlias().getName();
                String localField = null, foreignField = null ;

                if(!StringUtils.isEmpty(currTableAlias)){
                    // 关联表
                    String leftTableName = left.getTable().getName();
                    String rightTableName = right.getTable().getName();

                    // 当出现三表关联时 考虑关联第三张表是主表关联的还是第二张表关联的
                    if(leftTableName.contains(majorTableAlias) || rightTableName.contains(majorTableAlias)){
                        // 使用主表关联  localField: 源集合中的match值 ,bookListId, foreignField: 待Join的集合的match值 id
                        // 源集合 就是主表
                        if(currTableAlias.equals(leftTableName)){
                            foreignField = left.getColumnName();
                            localField = right.getColumnName();
                        } else if(currTableAlias.equals(rightTableName)){
                            foreignField = right.getColumnName();
                            localField = left.getColumnName() ;
                        }

                    } else {
                        // 使用中间表关联
                        // 如果被关联表有别名 那么 别名.字段 的 是外键
                        foreignField = left.getColumnName().contains(currTableAlias) ? left.getColumnName() : right.getColumnName();
                        localField = !left.getColumnName().contains(currTableAlias) ? left.getColumnName() : right.getColumnName();
                        // 关联第三张表时 用第二张表去关联 拼接第二张表名称
                        // 例如： from tab1 t1 left on tab2 t2 on t1.id = t2.t1_id left join tab3 t3 on t2.id = t3.t2_id
                        // 这里 tab3 是和 tab2 关联 所以 localField = tab2.id , mongo 是这样的
                        localField = table.getName().concat(".").concat(localField);
                    }
                } else {
                    // 没有别名 按照默认习惯  主键在前，外键在后
                    localField = left.getColumnName();
                    foreignField = right.getColumnName();
                }
                String as = "tmp_".concat(table.getName());

                lookUpData.add(LookUpData.builder()
                        .table(table.getName()).localField(localField)
                        .foreignField(foreignField).function(function)
                        .conversionFieldTable(conversionFieldTable)
                        .alias(currTableAlias).as(as).build());
            }
        }
    }
    return lookUpData;
}
```

#### 解析 where 条件
where 条件的解析是SQL解析中比较繁琐了，因为解析出的表达式有优先级关系，举个例子：
```
where b.title like ? and (b.style = ? or b.shelvesTime > ?)
```
这个条件，我们一眼就看出，先算括号里面的，然后再算括号外面的，所以整个优先级如下：

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c07c287a50be4480935748b28acc4c53~tplv-k3u1fbpfcp-watermark.image?)  
优先级高的要先计算，可以看到 `b.style = ?` 和  `b.shelvesTime > ?` 优先级最高，然后看到 `or` 和`b.title like ?` 优先级是一样的，这其实是  `b.title like ?` 和 `or`之后的结果优先级一样，`and` 优先级最后，所以同样的 优先级，操作符优先处理。

来看看解析 where 条件想解析出来的结果是啥样的
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/91df4ddf51e54b6db11bbc0c76eded54~tplv-k3u1fbpfcp-watermark.image?)

而这个表达式又分为两种，一种是 操作符，另外一种是 关系表达式
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/96c8a97de5804a69b382f73704c99b07~tplv-k3u1fbpfcp-watermark.image?)
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/eafc6daf959e4c1f98c01ff3f22ecc03~tplv-k3u1fbpfcp-watermark.image?)
关系表达式有个字段 value 是一个具体的值，当然SQL里面可以是另一个表的字段，但是这里并不支持。

贴一下解析 Where 的代码
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/78ffecf934ee45a5ad5d72643837dd9b~tplv-k3u1fbpfcp-watermark.image?)
最关键的 `MatchExpressionVisitorAdapter`
```
public class MatchExpressionVisitorAdapter extends ExpressionVisitorAdapter {

    private List<MatchData> items = new ArrayList<>();

    private int priority = 0;

    private ParserPart part;

    public List<MatchData> getItems() {
        return items;
    }

    public MatchExpressionVisitorAdapter() {}

    public MatchExpressionVisitorAdapter(ParserPart part){
        this.part = part;
    }

    public void processLogicalExpression(BinaryExpression expr, String logic) {
        MatchData.OperatorExpressionItem item = new MatchData.OperatorExpressionItem(logic);
        items.add(new MatchData(priority, 0, true, item));

        priority++;
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
        if (priority != 0) {
            priority--;
        }
    }

    @Override
    protected void visitBinaryExpression(BinaryExpression expr) {
        if (expr instanceof ComparisonOperator || expr instanceof LikeExpression) {

            if (!(expr.getLeftExpression() instanceof Column) && ParserPart.where == part) {
                //报错 暂不支持 where 不支持 函数
                throw new NotSupportFunctionException("where 条件不支持函数操作");
            }

            Expression expression = expr.getLeftExpression();
            AggregationFunction function = null;
            if(expr.getLeftExpression() instanceof Function && ParserPart.having == part){
                Function leftFunction = (Function) expr.getLeftExpression();
                function = AggregationFunction.parser(leftFunction.getName());
                ExpressionList parameters = leftFunction.getParameters();
                List<Expression> expressions = parameters.getExpressions();
                SqlSupportedSyntaxCheckUtil.checkFunctionColumn(expressions);
                // 解析出 函数 和 字段
                expression = expressions.get(0);
            }

            if( expression instanceof Column){
                Column leftColumn = Column.class.cast(expression);
                String tableAlias = leftColumn.getTable() == null ? null : leftColumn.getTable().getName();
                MatchData.RelationExpressionItem item = new MatchData.RelationExpressionItem(tableAlias, leftColumn.getColumnName(),
                        expr.getStringExpression(), function, SqlCommonUtil.handleExpressionValue(expr.getRightExpression()));
                items.add(new MatchData(priority, 1, false, item));
            }
        }
        super.visitBinaryExpression(expr);
    }

    @Override
    public void visit(AndExpression expr) {
        processLogicalExpression(expr, "AND");

    }

    @Override
    public void visit(OrExpression expr) {
        processLogicalExpression(expr, "OR");
    }

    @Override
    public void visit(InExpression expr) {

        if (!(expr.getLeftExpression() instanceof Column)) {
            //报错 暂不支持
            //  throw new RuntimeException()
        }

        ItemsList rightItemsList = expr.getRightItemsList();
        if (rightItemsList instanceof ExpressionList) {

            List<Object> valueList = new ArrayList<>();
            ExpressionList expressionList = (ExpressionList) rightItemsList;
            List<Expression> expressions = expressionList.getExpressions();
            expressions.stream().forEach(expression -> valueList.add(SqlCommonUtil.handleExpressionValue(expression)));

            Column leftColumn = Column.class.cast(expr.getLeftExpression());
            String tableAlias = leftColumn.getTable() == null ? null : leftColumn.getTable().getName();
            MatchData.RelationExpressionItem item = new MatchData.RelationExpressionItem(tableAlias, leftColumn.getColumnName(),
                    "IN", null, valueList);
            items.add(new MatchData(priority, 1, false, item));
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }


    /**
     *  解析部位 where 或者 having
     */
    public enum ParserPart {
        where,
        having;
    }
}
```
Having 的解析和where条件差不多，所以我直接复用了where的解析方式。

#### 解析SQL查询字段
查询字段对应 Mongo 的投影操作，先看看 mongo 的投影语法：
假设两张表，一个是 学生表（student），另一个是联系表（contact）
```
db.getCollection("student").aggregate([
    {
        $lookup:{
            from:"contact",
            localField: "_id",
            foreignField: "studentId",
            as: "contact"
        }
    },
    {
        $project:{// 字段映射
            name:1,
            ages:"$age",// 取别名，&age一定要""包围
            "contact.phone":1// 内嵌文档字段展示，contact.phone一定要用""包围
        }
    },
])
```
上面相当于 SQL ：

```
select s.name, s.age as ages, c.phone form student s left join contact c on s.id = c.studentId
```
当投影的是被关联表的字段，需要注意投影时要携带被关联表的 as，其实很好理解，就是被关联表的数据查询出来放在 as 的结果集里，比如上面的 contact，那你查的时候，肯定要 “contact.字段” 形式。

所以再来看看，解析 查询字段都需要解析出什么数据
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/43de18ef8f7341eda67b23510283a082~tplv-k3u1fbpfcp-watermark.image?)  
可以看到这里解析的结果没有 as 啊，确实没有，因为 as 存在表关联中，在解析 Join 的时候就已经解析了，我们这里有表名可以找到是对应的as

贴一下源代码
```
public static List<ProjectData> parser(List<SelectItem> selectItems) {

    List<ProjectData> projects = new ArrayList<>();
    for (SelectItem selectItem : selectItems) {
        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
        // 别名
        String alias = selectExpressionItem.getAlias() == null ? "" : selectExpressionItem.getAlias().getName();

        Expression expression = selectExpressionItem.getExpression();
        //判断表达式是否是函数

        String functionName = null;
        if (expression instanceof Function) {
            Function function = (Function) expression;
            functionName = function.getName();
            SqlSupportedSyntaxCheckUtil.checkProjectSupportFunction(functionName);
            ExpressionList parameters = function.getParameters();
            List<Expression> expressions = parameters.getExpressions();

            SqlSupportedSyntaxCheckUtil.checkFunctionColumn(expressions);

            // 解析出 函数 和 字段
            expression = expressions.get(0);
        }


        if (expression instanceof Column) {

            Column column = Column.class.cast(expression);

            String columnName = column.getColumnName();
            alias = StringUtils.isNotBlank(alias) ? alias : columnName;

            String table = null;
            if(ObjectUtils.isNotEmpty(column.getTable())){
                table = column.getTable().getName();
            }

            projects.add(ProjectData.builder().alias(alias).table(table).field(columnName)
                    .function(AggregationFunction.parser(functionName)).build());
        }


    }
    return projects;
}
```

#### 解析SQL语句中 Group By
解析分组比较简单，只要解析出 分组字段名和该字段是哪个表的表别名
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/13ec0b3dcd164bb8905a9a00deaa2b8c~tplv-k3u1fbpfcp-watermark.image?)

#### 解析 SQL 语句 排序部分
解析排序也很简单，只要解析出哪个字段排序以及排序的方式
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7619f259e2b84951a2f79eea6d5c4a46~tplv-k3u1fbpfcp-watermark.image?)

#### 解析 SQL 语句 Limit 分页
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f8e442c1358846dea1cf28ddc019be5e~tplv-k3u1fbpfcp-watermark.image?)

### 组装 Mongo API
解析完SQL之后就得到发给Mongo的元数据了，就利用 mongoTemplate 的 聚合API aggregate 填充参数查询数据了，值得注意的是，**解析SQL语句是没有顺序限制的，先解析那部分都可以，但是 Mongo 查询是有顺序的，因为它是管道操作，管道我们都知道后面的操作是基于前面的操作结果的**。

所以，我这里处理顺序是，先处理表关联（lookUp)，然后过滤（match），分组（group)，having（having 其实是过滤操作 match），排序（order），分页（limit/skip），投影（project）

//图

#### 处理表关联 （Lookup)
再来看看 Mongo 表关联的 API

```
// 以grade为主表，左连接student表
db.getCollection("grade").aggregate([
    {
        $lookup:{
            from:"student",// 连接表
            localField: "_id",// 主表关联字段
            foreignField: "gradeId",// 连接表关联字段
            as: "students"// 别名，数组形式显示
        }
    },
])
```

下图是 分析 Join 的代码
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/378cc5ded3964f67b5a11259f6f1db10~tplv-k3u1fbpfcp-watermark.image?)
可以看到分析 Join 的时候，需要传入投影字段（select 查询的字段），先解释一下，封装Join API需要投影的字段：

这是因为在做表关联时，有时需要转化函数，前面介绍解析Join时也说到了。用到转化函数，就需要把这个字段先查出来，就需要先投影出字段，然而 Mongo 的管道操作限制，如果只将转化函数的那个字段投影出来，那么后面想投影出其他字段就不行。举个例子：

```
select t1.id, t1.name, t2.title form tab1 t1 join tab2 t2 on string(t1.id) = t2.tid;
```
上面的语句中，由于使用 string 函数把ti.id的数值转为 string 类型，所以就需要先把 t1.id 查出来（投影），但是如果不把 t1.name 投影出来，那后面就无法投影这个字段了。

> 如果不好理解可以先看看管道的概念：  
> 管道在Unix和Linux中一般用于将当前命令的输出结果作为下一个命令的参数。  
> MongoDB的聚合管道将MongoDB文档在一个管道处理完毕后将结果传递给下一个管道处理。管道操作是可以重复的
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f6d832dd037344a09179b12f923c1c19~tplv-k3u1fbpfcp-watermark.image?)


> 还有一点需要注意的是 :
>
> 在做表关联之前只能投影主表的字段，也就是转化函数只能作用在主表的字段中。由上面 Mongo 表关联的 API 得知，想要投影从表的字段，那得先把从表关联查询，将结果存入 as 里面，但是想要关联从表，因为有转化函数，所以就需要先投影从表字段...... 这就变成 蛋生鸡、鸡生蛋问题了。
>
> 那怎么办呢？所以这里提供两个函数 ：**objectId**、**string**  
>  如果要用 主表的 ID去关联，那就将主表 ID投影出来 用 string 函数转化  
>  如果要用 主表的 外键去关联，那就将主表的 外键 投影出来 用 ObjectId 函数转化
>

#### 处理过滤操作
Mongo中过滤的语法：`{ $match: { <query> } }`

过滤操作要稍微复杂点，一条SQL语句中多个 where 条件是有优先级的，比如下面这个SQL：
```
where b.title like ? and (b.style = ? or b.shelvesTime > ?)
```
先处理 `b.style = ?` 然后处理  `b.shelvesTime > ?`，将它们处理的结果集再与 `b.title like ?` 做 `and` 操作。

我们在解析 where 条件的时候就已经解析出表达式和操作符的优先级了，并且按照优先级排序。

上面这种操作很像以前开发 计算器的功能，比如： `1 + 2 * 3 + (3-6)`，这里也有运算优先级，也有操作符。
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f6596a024c5e4dca9c1c12044155c9ef~tplv-k3u1fbpfcp-watermark.image?)

**解决思路：**  
利用**栈的先进后出结构**，创建一个栈，存放一个 `Criteria` ，即表达式过滤。如果遇到表达式就解析成 `Criteria` 入栈，如果遇到操作符（and、or)，就从栈中取出两个 `Criteria`，然后运算成一个新的 `Criteria` 入栈，以此类推，最后取出栈中第一个 `Criteria`，就是过滤的条件了。

分析过滤的代码
```
private static List<AggregationOperation> analysisMatch(String majorTableAlias, List<MatchData> matchData, Map<String, LookUpData> lookUpDataMap) {
    List<AggregationOperation> operations = new ArrayList<>();
    Deque<Criteria> stack = new ArrayDeque<>();
    if (!CollectionUtils.isEmpty(matchData)) {
        // matchData 已经安装优先级排序了 这里不需要处理优先级
        matchData.stream().forEach(matchDataItem -> {
            if (!matchDataItem.getIsOperator()) {
                // 不是操作符 入栈
                MatchData.RelationExpressionItem expression = (MatchData.RelationExpressionItem) matchDataItem.getExpression();
                String operator = expression.getOperator();
                String field = expression.getField();
                String tableAlias = expression.getTableAlias();
                Object paramValue = expression.getValue();
                LookUpData lookUpData = lookUpDataMap.get(tableAlias);

                if (!majorTableAlias.equals(tableAlias) && lookUpData != null) {
                    field = lookUpData.getAs().concat(".").concat(field);
                }

                Criteria where = Criteria.where(field);
                if ("=".equals(operator)) {
                    where.is(paramValue);
                } else if ("LIKE".equalsIgnoreCase(operator)) {
                    Pattern pattern = Pattern.compile("^.*" + paramValue + ".*$", 2);
                    where.regex(pattern);
                } else if ("IN".equalsIgnoreCase(operator)) {
                    if (paramValue instanceof Collection) {
                        Collection collection = (Collection) paramValue;
                        where.in(collection.toArray(new Object[collection.size()]));
                    }
                } else if ("<".equals(operator)) {
                    where.lt(paramValue);
                } else if ("<=".equals(operator)) {
                    where.lte(paramValue);
                } else if (">".equals(operator)) {
                    where.gt(paramValue);
                } else if (">=".equals(operator)) {
                    where.gte(paramValue);
                } else if ("!=".equals(operator) || "<>".equals(operator)) {
                    where.ne(paramValue);
                }
                stack.push(where);
            } else {
                MatchData.OperatorExpressionItem expression = MatchData.OperatorExpressionItem.class.cast(matchDataItem.getExpression());
                // 操作符 取出两个 操作数 来计算
                Criteria left = stack.pop();
                Criteria right = stack.pop();
                Criteria result = null;
                if ("AND".equalsIgnoreCase(expression.getOperatorName())) {

                    result = left.andOperator(right);
                } else if ("OR".equalsIgnoreCase(expression.getOperatorName())) {
                    result = left.orOperator(right);
                }
                stack.push(result);
            }
        });
    }

    if (!stack.isEmpty()) {
        Criteria criteria = stack.getFirst();
        operations.add(Aggregation.match(criteria));
    }

    return operations;
}
```

#### 处理分组（group）
Mongo 的分组语法：   
`{ $group: { _id: <expression>, <field1>: { <accumulator1> : <expression1> }, ... } }`


再来看看 `mongoTemplate.aggregate` 聚合API
```
Aggregation.group(String... fields)
            .count().as("别名")
            .sum(field).as("别名")
            .....
```
由 `mongoTemplate` 聚合 API 可知，分组字段之后就是处理聚合函数，那一句SQL中，聚合函数会出现在哪里呢？基本上出现在 **查询字段**以及 **having** 上。

所以这里要做的就是，将 having 中出现的聚合函数以及投影（select 查询字段）出现的聚合函数都放到分组里面处理。


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d703212e6dc949438a4c5090bf9c51d8~tplv-k3u1fbpfcp-watermark.image?)

分组代码
```
private static List<AggregationOperation> analysisGroup(List<ProjectData> projectData, List<GroupData> groupData, Map<String, LookUpData> lookUpDataMap) {
    List<AggregationOperation> operations = new ArrayList<>();
    if (!CollectionUtils.isEmpty(groupData)) {

        List<ProjectData> functionProjectData = projectData.stream().filter(item -> ObjectUtils.isNotEmpty(item.getFunction())).collect(Collectors.toList());

        Map<AggregationFunction, List<ProjectData>> functionProjectDataMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(functionProjectData)) {
            functionProjectDataMap.putAll(functionProjectData.stream().collect(Collectors.groupingBy(ProjectData::getFunction)));
        }

        List<String> groupFields = new ArrayList<>();
        groupData.stream().forEach(group -> {

            String field = group.getField();

            if (StringUtils.isNotEmpty(group.getTableAlias())) {
                // 被关联的表 需要有 结果集 as 的前缀
                LookUpData lookUpData = lookUpDataMap.get(group.getTableAlias());
                if (ObjectUtils.isNotEmpty(lookUpData)) {
                    field = lookUpData.getAs().concat(".").concat(field);
                }
            }
            groupFields.add(field);
        });

        AtomicReference<GroupOperation> groupOperation = new AtomicReference<>(Aggregation.group(groupFields.toArray(new String[groupFields.size()])));
        functionProjectDataMap.forEach((function, projectDataList) -> {
            if (AggregationFunction.SUM == function) {
                // 求和
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().sum(field).as(alias)));
            } else if (AggregationFunction.AVG == function) {
                // 求平均
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().avg(field).as(alias)));
            } else if (AggregationFunction.MIN == function) {
                // 最小值
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().min(field).as(alias)));
            } else if (AggregationFunction.MAX == function) {
                // 最大值
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().max(field).as(alias)));
            } else if (AggregationFunction.FIRST == function) {
                // 分组和第一个
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().first(field).as(alias)));
            } else if (AggregationFunction.LAST == function) {
                // 分组 最后一个
                handleAggregationFunction(lookUpDataMap, projectDataList, (field, alias) -> groupOperation.set(groupOperation.get().last(field).as(alias)));
            }
        });
        operations.add(groupOperation.get());
    }

    return operations;
}
```
#### 处理 Having
SQL 中的having其实也是过滤，所以之间调用 where 模块的处理方式。

#### 处理排序和分页

排序和分页比较简单，直接贴代码
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3f6e2f5239954857a8b9ddbc9d2b508d~tplv-k3u1fbpfcp-watermark.image?)

#### 处理投影（查询字段）

投影也比较简单，不过要注意两点：
1. 第一点注意的是，mongo 里面投影有个问题，只有一个 group by 的话 ，对这个 group by 字段投影会失败。  
   举个例子：
    ```
    select n.channel as channel, avg(n.score) as avgScore from novel n group by n.channel
    ```
   最后翻译成 Mongo API  
   ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f2341d31bcee4d6c9cb4a1f9630522d8~tplv-k3u1fbpfcp-watermark.image?)

   对多个字段分组
    ```
    select n.channel as channel, n.enable, avg(n.score) as avgScore from novel n group by n.channel, n.enable
    ```
   最后翻译成 Mongo 语法：
   ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d21242ec36ee485f99f21dbe74eaa3a2~tplv-k3u1fbpfcp-watermark.image?)

   > 这也不知道是啥原因，也可能是我 Mongo 用的不对，知道的老铁请留言告知

   所以我这里做了一下特殊处理：
   ![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/063c24b2e38a4ee3895b65fd898f3c44~tplv-k3u1fbpfcp-watermark.image?)
   大体意思就是，如果只有一个分组字段，并且这个字段还需要投影，那么投影的字段就引用 _id。

   最后翻译的 Mongo 就长这样了
   ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4115e6496c1248bda3d0d6cbb14e8740~tplv-k3u1fbpfcp-watermark.image?)

2. 第二点要注意的是，投影被关联表的字段时，字段需要携带被关联表的 as 值。  
   举个列子：

    ```
    select b.title, b.remark, c.bookListId, c.moduleName from bookList b left join categoryModuleRecommend c on c.bookListId = string(b._id)  where b.title like ? and c.bookListId != null
    ```

   最后翻译的 Mongo 语法：

   ![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a8b92619e5a7444da546db6a331c9a0a~tplv-k3u1fbpfcp-watermark.image?)  
   ![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/92325c6c56bd4949a1b08beb80090c80~tplv-k3u1fbpfcp-watermark.image?)

最后贴一个投影的代码
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e60cd8bcbac14c9c9ec21ef1da01629e~tplv-k3u1fbpfcp-watermark.image?)

## 如何使用
1. 导入依赖
    ```
    <dependency>
        <groupId>com.rrtv</groupId>
        <artifactId>sqltomongo-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    ```

   如果使用下面方式一，这样配置就可以了，不过要确保，项目中有 `MongoTemplate` 这个Bean。


3. 方式一： 可以直接注入 `SQLToMongoTemplate` 这个Bean 来使用，例如：
   ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/977f27cc8ee24eaeae6de3d8da0695b0~tplv-k3u1fbpfcp-watermark.image?)

3. 方式二：类似 MyBatis的使用方式

    - 启动类增加注解：  
      `@EnableSqlToMongoMapper(basePackages = "xxxx")`

    - 编写 Mapper 接口
      ![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7472c179833b4198878a3c3abdc84f47~tplv-k3u1fbpfcp-watermark.image?)

    - 编写 Mapper.xml
      ![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7f06d8c52d5345f1bff8fc0008df1bf2~tplv-k3u1fbpfcp-watermark.image?)
      Mapper.xml 的默认扫描路径：`classpath*:mapper/*.xml`，可以通过 `sql-to-mongo.mapper.base-package` 修改扫描路径。
      ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bcf7f531516644fdb1b0613d6d7133af~tplv-k3u1fbpfcp-watermark.image?)

   运行结果
   ![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a1528ebe5730418b90d5a19fa1f916bc~tplv-k3u1fbpfcp-watermark.image?)

