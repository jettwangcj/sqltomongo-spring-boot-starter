package com.rrtv.analyzer;

import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.parser.data.SortData;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Classname SortAnalyzer
 * @Description
 * @Date 2022/8/12 11:55
 * @Created by wangchangjiu
 */
public class SortAnalyzer extends AbstractAnalyzer {

    @Override
    public void proceed(List<AggregationOperation> operations, PartSQLParserData data) {
        List<SortData> sortData = data.getSortData();
        // 分析 排序  构建 mongo API
        if (!CollectionUtils.isEmpty(sortData)) {
            sortData.stream().forEach(sort -> operations.add(Aggregation.sort(sort.getDirection(), sort.getField())));
        }
    }


}
