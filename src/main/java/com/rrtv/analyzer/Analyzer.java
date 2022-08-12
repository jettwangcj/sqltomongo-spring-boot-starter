package com.rrtv.analyzer;

import com.rrtv.parser.data.PartSQLParserResult;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * @Classname Analyzer
 * @Description
 * @Date 2022/8/12 11:51
 * @Created by wangchangjiu
 */
public interface Analyzer<T> {

   void proceed(List<AggregationOperation> operations, PartSQLParserResult<T> partSQLParserResult);

}
