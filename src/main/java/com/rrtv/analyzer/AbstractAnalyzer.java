package com.rrtv.analyzer;

import com.rrtv.parser.data.PartSQLParserData;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * @Classname Analyzer
 * @Description
 * @Date 2022/8/12 11:51
 * @Created by wangchangjiu
 */
public abstract class AbstractAnalyzer implements Analyzer {

    protected Analyzer checker = null;

    @Override
    public void setNextAnalyzer(Analyzer checker) {
        this.checker = checker;
    }

    @Override
    public void analysis(List<AggregationOperation> operations, PartSQLParserData data) {
        this.proceed(operations, data);
        if (this.checker != null) {
            // 责任链设计模式 ， 传递给下一个分析器
            checker.analysis(operations, data);
        }
    }

    @Override
    public abstract void proceed(List<AggregationOperation> operations, PartSQLParserData data);

    public static class Builder {
        private Analyzer head;
        private Analyzer tail;

        // 添加处理者
        public Builder addAnalyzer(Analyzer chain) {
            if (this.head == null) {
                this.head = this.tail = chain;
                return this;
            }
            // 设置下一个处理者
            this.tail.setNextAnalyzer(chain);
            this.tail = chain;
            return this;
        }

        public Analyzer build() {
            return this.head;
        }
    }

}
