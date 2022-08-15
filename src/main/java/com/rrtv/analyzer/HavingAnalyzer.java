package com.rrtv.analyzer;

import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.MatchData;
import com.rrtv.parser.data.PartSQLParserData;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname HavingAnalyzer
 * @Description
 * @Date 2022/8/12 11:53
 * @Created by wangchangjiu
 */
public class HavingAnalyzer extends AbstractAnalyzer {


    @Override
    public void proceed(List<AggregationOperation> operations, PartSQLParserData data) {

        List<MatchData> havingData = data.getHavingData();
        String majorTableAlias = data.getMajorTableAlias();
        List<LookUpData> joinParser = data.getJoinParser();

        // 别名和表的映射
        Map<String, LookUpData> lookUpDataMap = joinParser.stream().collect(Collectors.toMap(LookUpData::getAlias, Function.identity()));

        // 分析 having
        if (!CollectionUtils.isEmpty(havingData)) {
            operations.addAll(analysisMatch(majorTableAlias, havingData, lookUpDataMap));
        }

    }

    /**
     * 分析 过滤 匹配条件 构建 mongo 过滤 API
     *
     * @param majorTableAlias
     * @param matchData
     * @param lookUpDataMap
     * @return
     */
    private List<AggregationOperation> analysisMatch(String majorTableAlias, List<MatchData> matchData, Map<String, LookUpData> lookUpDataMap) {
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

}
