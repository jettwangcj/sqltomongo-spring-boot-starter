package com.rrtv.parser;

import com.alibaba.fastjson.JSON;
import com.rrtv.analyzer.Analyzer;
import com.rrtv.common.MongoParserResult;
import com.rrtv.common.ParserPartTypeEnum;
import com.rrtv.orm.Configuration;
import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.util.SqlCommonUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
        data.setMajorTableAlias(SqlCommonUtil.getMajorTableAlias(plain));

        Stream.of(ParserPartTypeEnum.values()).forEach(item -> {
            // 开始解析SQL各个部分
            configuration.newPartSQLParser(item).proceedData(plain, data);
        });

        // ====== 下面开始 分析 各个部分 构建 Mongo API ============
        List<AggregationOperation> operations = new ArrayList<>();

        // 使用责任链设计模式开始分析 每个部分 SQL 封装 MongoAPI
        Analyzer analyzer = configuration.newAnalyzer();
        analyzer.analysis(operations, data);

        Aggregation aggregation = Aggregation.newAggregation(operations);
        if (logger.isInfoEnabled()) {
            logger.info("build Aggregation : " + JSON.toJSONString(aggregation));
        }
        Table table = Table.class.cast(plain.getFromItem());
        mongoParserResult = new MongoParserResult(aggregation, table.getName());
        return mongoParserResult;
    }


}
