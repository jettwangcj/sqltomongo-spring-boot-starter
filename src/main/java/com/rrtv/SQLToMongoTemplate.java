package com.rrtv;

import com.rrtv.common.MongoParserResult;
import com.rrtv.parser.SelectSQLTypeParser;
import com.rrtv.util.SqlCommonUtil;
import com.rrtv.util.SqlParameterSetterUtil;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.lang.Nullable;

import java.util.List;

public class SQLToMongoTemplate {

    private MongoTemplate mongoTemplate;

    public SQLToMongoTemplate(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }


    public <T> T selectOne(String sql, Class<T> returnType, @Nullable Object... parameters) {
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


    public <T> List<T> selectList(String sql, Class<T> returnType, @Nullable Object... parameters) {
        SqlSupportedSyntaxCheckUtil.checkSqlType(sql, SqlCommonUtil.SqlType.SELECT);
        sql = SqlParameterSetterUtil.parameterSetter(sql, parameters);
        MongoParserResult result = SelectSQLTypeParser.parser(sql);
        AggregationResults<T> results = mongoTemplate.aggregate(result.getAggregation(), result.getCollectionName(), returnType);
        return results.getMappedResults();
    }


}
