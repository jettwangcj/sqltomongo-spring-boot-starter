package com.rrtv.analyzer;

import com.rrtv.parser.data.LimitData;
import com.rrtv.parser.data.PartSQLParserData;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;

import java.util.List;

/**
 * @Classname SortAnalyzer
 * @Description
 * @Date 2022/8/12 11:55
 * @Created by wangchangjiu
 */
public class LimitAnalyzer extends AbstractAnalyzer {

    @Override
    public void proceed(List<AggregationOperation> operations, PartSQLParserData data) {
        LimitData limitData = data.getLimitData();

        // 分页  构建 mongo API
        if (ObjectUtils.isNotEmpty(limitData)) {
            SkipOperation skipOperation = Aggregation.skip(limitData.getOffsetValue() * limitData.getRowCount());
            LimitOperation limitOperation = Aggregation.limit(limitData.getRowCount());
            operations.add(skipOperation);
            operations.add(limitOperation);
        }
    }


}
