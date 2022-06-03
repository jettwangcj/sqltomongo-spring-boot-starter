package com.rrtv.common;

import org.apache.commons.lang3.StringUtils;

public enum AggregationFunction {

    COUNT("count"),
    SUM("sum"),
    AVG("avg"),
    MIN("min"),
    MAX("max"),
    FIRST("first"),
    LAST("last");

    private String code;

    AggregationFunction(String code) {
        this.code = code;
    }

    public static AggregationFunction parser(String code) {
        if(StringUtils.isEmpty(code)){
            return null;
        }

        for (AggregationFunction function : AggregationFunction.values()) {
            if (function.code.equalsIgnoreCase(code)) {
                return function;
            }
        }
        return null;
    }


}
