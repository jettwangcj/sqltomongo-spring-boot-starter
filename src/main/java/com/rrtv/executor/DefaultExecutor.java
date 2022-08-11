package com.rrtv.executor;

import com.rrtv.common.MongoParserResult;
import com.rrtv.parser.SelectSQLTypeParser;
import com.rrtv.util.SqlCommonUtil;
import com.rrtv.util.SqlParameterSetterUtil;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

/**
 * @Classname DefaultExecutor
 * @Description
 * @Date 2022/8/9 16:57
 * @Created by wangchangjiu
 */
public class DefaultExecutor implements Executor {

    private MongoTemplate mongoTemplate;

    public DefaultExecutor(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public <T> T selectOne(String sql, Class<T> returnType, Object... parameters) {
        // 检查 SQL 是否是 SELECT 语句
        SqlSupportedSyntaxCheckUtil.checkSqlType(sql, SqlCommonUtil.SqlType.SELECT);
        // 设置参数
        sql = SqlParameterSetterUtil.parameterSetter(sql, parameters);
        // 解析 SQL 并返回封装 Mongo 的API
        MongoParserResult result = SelectSQLTypeParser.parser(sql);
        // 使用 MongoTemplate 的 aggregate 聚合查询 API 获取结果
        AggregationResults<T> results = mongoTemplate.aggregate(result.getAggregation(),
                result.getCollectionName(), returnType);
        return results.getUniqueMappedResult();
    }

    @Override
    public <T> List<T> selectList(String sql, Class<T> returnType, Object... parameters) {
        SqlSupportedSyntaxCheckUtil.checkSqlType(sql, SqlCommonUtil.SqlType.SELECT);
        sql = SqlParameterSetterUtil.parameterSetter(sql, parameters);
        MongoParserResult result = SelectSQLTypeParser.parser(sql);
        AggregationResults<T> results = mongoTemplate.aggregate(result.getAggregation(), result.getCollectionName(), returnType);
        return results.getMappedResults();
    }
}
