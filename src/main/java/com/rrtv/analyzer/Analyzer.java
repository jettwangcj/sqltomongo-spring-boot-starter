package com.rrtv.analyzer;

import com.rrtv.parser.data.PartSQLParserData;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * @Classname Analyzer
 * @Description
 * @Date 2022/8/15 16:24
 * @Created by wangchangjiu
 */
public interface Analyzer {

    void setNextAnalyzer(Analyzer checker);

    void analysis(List<AggregationOperation> operations, PartSQLParserData data);

    void proceed(List<AggregationOperation> operations, PartSQLParserData data);

}
