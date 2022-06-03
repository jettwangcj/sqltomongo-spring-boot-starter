package com.rrtv.parser.data;

import com.rrtv.common.AggregationFunction;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MatchData implements Serializable {

    /**
     *  解析优先级
     */
    private int priority;

    // 排序 用户后面 Java 8 对List 先按优先级排序 再按 sort 排序，
    // 但是测试之后 貌似 两次排序不能同时 降序，必须一个降序一个升序，所以加了一个字段 恶心一把
    private int sort;

    /**
     *  是否是操作符
     */
    private Boolean isOperator;

    /**
     *  表达式
     */
    private ExpressionItem expression;

    public MatchData(){}

    public MatchData(int priority, int sort, boolean isOperator, ExpressionItem expression){
        this.priority = priority;
        this.sort = sort;
        this.isOperator = isOperator;
        this.expression = expression;
    }


    interface ExpressionItem {}

    @Data
    public static class OperatorExpressionItem implements ExpressionItem {

        private String operatorName;
        public OperatorExpressionItem(){}
        public OperatorExpressionItem(String operatorName){
            this.operatorName = operatorName;
        }

    }

    // 关系表达式 列入 b.style = ?
    @Data
    public static class RelationExpressionItem implements ExpressionItem {
        /**
         *  表别名
         */
        private String tableAlias;
        /**
         *  字段
         */
        private String field;
        /**
         *  关系操作符 =、 >、 <
         */
        private String operator;
        /**
         *  值
         */
        private Object value;
        /**
         *  聚合函数
         */
        private AggregationFunction function;

        public RelationExpressionItem(){}

        public RelationExpressionItem(String tableAlias, String field, String operator, AggregationFunction function,  Object value){
            this.tableAlias = tableAlias;
            this.field = field;
            this.operator = operator;
            this.function = function;
            this.value = value;
        }

    }




}
