package com.rrtv.parser;

import com.alibaba.fastjson.JSON;
import com.rrtv.analyzer.Analyzer;
import com.rrtv.common.AggregationFunction;
import com.rrtv.common.MongoParserResult;
import com.rrtv.common.ParserPartTypeEnum;
import com.rrtv.exception.SqlParserException;
import com.rrtv.orm.Configuration;
import com.rrtv.parser.data.*;
import com.rrtv.util.SqlCommonUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectSQLTypeParser {

    private Configuration configuration;

    private static final Log logger = LogFactory.getLog(SelectSQLTypeParser.class);

    private static Map<String, MongoParserResult> parserCache = new ConcurrentHashMap<>();


    public SelectSQLTypeParser(Configuration configuration){
        this.configuration = configuration;
    }

    public MongoParserResult parser(String sql) {

        MongoParserResult mongoParserResult = parserCache.get(sql);
        if (mongoParserResult != null) {
            return mongoParserResult;
        }

        PlainSelect plain = SqlCommonUtil.parserSelectSql(sql);

        // 解析后的数据
        PartSQLParserData data = new PartSQLParserData();
        Stream.of(ParserPartTypeEnum.values()).forEach(item -> {
            // 开始解析SQL各个部分
            configuration.newPartSQLParser(item).proceedData(plain, data);
        });

        // ====== 下面开始 分析 各个部分 构建 Mongo API ============
        List<AggregationOperation> operations = new ArrayList<>();

        Analyzer analyzer = configuration.newAnalyzer();
        analyzer.analysis(operations, data);


       /* // 分析 join 构建 mongo API
        Map<String, List<ProjectData>> projectMap = projectData.stream().collect(Collectors.groupingBy(ProjectData::getTable));
        operations.addAll(analysisJoin(joinParser, projectMap));

        // 别名和表的映射
        Map<String, LookUpData> lookUpDataMap = joinParser.stream().collect(Collectors.toMap(LookUpData::getAlias, Function.identity()));

        // 分析 匹配 过滤  构建 mongo API
        operations.addAll(analysisMatch(majorTableAlias, matchData, lookUpDataMap));

        // having 中出现的 聚合函数 也要计算出来  ProjectData 重写 equals 目的是去重
        Set<ProjectData> projectDataSet = new HashSet<>();
        projectDataSet.addAll(projectData);
        if (!CollectionUtils.isEmpty(havingData)) {
            havingData.stream().filter(match -> !match.getIsOperator()).forEach(item -> {
                MatchData.RelationExpressionItem expression = (MatchData.RelationExpressionItem) item.getExpression();
                projectDataSet.add(ProjectData.builder()
                        .table(expression.getTableAlias())
                        .field(expression.getField())
                        .function(expression.getFunction()).build());
            });
        }

        // 分析 分组 构建 Mongo API
        operations.addAll(analysisGroup(new ArrayList<>(projectDataSet), groupData, lookUpDataMap));

        // 分析 having
        if (!CollectionUtils.isEmpty(havingData)) {
            operations.addAll(analysisMatch(majorTableAlias, havingData, lookUpDataMap));
        }

        // 不知道为啥 mongo 里面投影有个问题 只有一个 group by 的话 ，对这个 group by 字段投影会失败 TODO 这里做特殊处理
        if (!CollectionUtils.isEmpty(groupData) && groupData.size() == 1) {
            String field = groupData.get(0).getField();
            projectData.stream().filter(project -> field.equals(project.getField())).findFirst()
                    .ifPresent(project -> project.setField("_id"));
        }

        // 分析 排序  构建 mongo API
        if (!CollectionUtils.isEmpty(sortData)) {
            sortData.stream().forEach(sort -> operations.add(Aggregation.sort(sort.getDirection(), sort.getField())));
        }

        // 分页  构建 mongo API
        if (ObjectUtils.isNotEmpty(limitData)) {
            SkipOperation skipOperation = Aggregation.skip(limitData.getOffsetValue() * limitData.getRowCount());
            LimitOperation limitOperation = Aggregation.limit(limitData.getRowCount());
            operations.add(skipOperation);
            operations.add(limitOperation);
        }

        // 分析投影  构建 mongo API
        operations.addAll(analysisProject(majorTableAlias, projectData, lookUpDataMap));*/


        Aggregation aggregation = Aggregation.newAggregation(operations);
        if (logger.isInfoEnabled()) {
            logger.info("build Aggregation : " + JSON.toJSONString(aggregation));
        }
        Table table = Table.class.cast(plain.getFromItem());
        mongoParserResult = new MongoParserResult(aggregation, table.getName());
        return mongoParserResult;
    }

    /**
     * 构建 Mongo 分组 API
     *
     * @param projectData
     * @param groupData
     * @param lookUpDataMap
     * @return
     */
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

    private static void handleAggregationFunction(Map<String, LookUpData> lookUpDataMap, List<ProjectData> projectDataList, BiConsumer<String, String> groupOperationBiConsumer) {
        projectDataList.stream().forEach(project -> {
            String field = project.getField();

            if (StringUtils.isNotEmpty(project.getTable())) {
                // 被关联的表 需要有 结果集 as 的前缀
                LookUpData lookUpData = lookUpDataMap.get(project.getTable());
                if (lookUpData != null) {
                    field = lookUpData.getAs().concat(".").concat(field);
                }
            }
            groupOperationBiConsumer.accept(field, field);
        });
    }

    /**
     * 分析 投影 构建 投影 Mongo API
     *
     * @param majorTableAlias
     * @param projectData
     * @param lookUpDataMap
     * @return
     */
    private static List<AggregationOperation> analysisProject(String majorTableAlias, List<ProjectData> projectData,
                                                              Map<String, LookUpData> lookUpDataMap) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Field> fields = projectData.stream().map(project -> {
            // 字段携带表别名 例如 select t1.id,t2.name from
            if (StringUtils.isNotEmpty(project.getTable())) {
                String table = project.getTable();
                if (table.equals(majorTableAlias)) {
                    // 主表投影
                    return Fields.field(project.getAlias(), project.getField());
                }

                // 被关联表 需要携带 as （被关联表数据集）
                LookUpData lookUpData = lookUpDataMap.get(project.getTable());
                return Fields.field(project.getField(), lookUpData.getAs().concat(".").concat(project.getField()));
            }

            // 没有 字段携带的 就是单表
            return Fields.field(project.getField(), project.getAlias());
        }).collect(Collectors.toList());
        operations.add(Aggregation.project(Fields.from(fields.toArray(new Field[fields.size()]))));
        return operations;
    }


    /**
     * 分析 过滤 匹配条件 构建 mongo 过滤 API
     *
     * @param majorTableAlias
     * @param matchData
     * @param lookUpDataMap
     * @return
     */
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

    /**
     * 分析 JOIN 构建 mongo 表关联 API
     *
     * @param joinParser
     * @param projectMap
     * @return
     */
    private static List<AggregationOperation> analysisJoin(List<LookUpData> joinParser, Map<String, List<ProjectData>> projectMap) {
        List<AggregationOperation> operations = new ArrayList<>();
        if (!CollectionUtils.isEmpty(joinParser)) {
            joinParser.stream().forEach(join -> {
                // 这里判断是否有携带函数的， 携带函数的，需要把那个表的字段先投影出来 在做 函数转换
                if (ObjectUtils.isNotEmpty(join.getFunction())) {
                    // 关联表 需要转换 例如 from tab1 t1 left join tab2 t2 on t1.id = ObjectId(t2.oid) 那么需要先投影
                    // select c.bookListId, c.lang, b.title from categoryModuleRecommend c left join bookList b on ObjectId(c.bookListId) = b.id
                    // 找出需要转换函数处理的是哪张表，投影出这张表的字段，包括关联字段
                    List<ProjectData> projectDataList = projectMap.get(join.getConversionFieldTable());
                    List<String> fields = Optional.of(projectDataList.stream().map(ProjectData::getField).collect(Collectors.toList())).orElse(new ArrayList<>());
                    // 关联字段不在 select 里面，把它包含进去，这个字段需要投影出来
                    String localField = join.getLocalField();
                    if (!fields.contains(localField)) {
                        fields.add(localField);
                    }
                    // 投影
                    operations.add(Aggregation.project(fields.toArray(new String[fields.size()]))
                            .and(ConvertOperators.Convert.convertValue("$".concat(localField)).to(join.getFunction().getCode())).as(localField));
                }
                // 关联表
                operations.add(Aggregation.lookup(join.getTable(), join.getLocalField(), join.getForeignField(), join.getAs()));
                // 展平
                operations.add(Aggregation.unwind(join.getAs(), true));
            });
        }
        return operations;
    }


}
