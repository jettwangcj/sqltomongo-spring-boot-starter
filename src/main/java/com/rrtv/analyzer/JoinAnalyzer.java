package com.rrtv.analyzer;

import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.PartSQLParserResult;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * @Classname JoinAnalyzer
 * @Description
 * @Date 2022/8/12 11:51
 * @Created by wangchangjiu
 */
public class JoinAnalyzer implements Analyzer<LookUpData> {

    @Override
    public void analysis(List<AggregationOperation> operations, PartSQLParserResult<LookUpData> partSQLParserResult) {

    }
}
