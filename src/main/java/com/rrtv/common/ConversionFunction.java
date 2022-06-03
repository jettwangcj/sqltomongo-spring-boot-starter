package com.rrtv.common;

public enum ConversionFunction {

    TO_OBJECT_ID("objectId"),
    TO_STRING("string");

    private String code;


    public String getCode() {
        return code;
    }

    ConversionFunction(String code) {
        this.code = code;
    }

    public static ConversionFunction parser(String code) {
        for (ConversionFunction function : ConversionFunction.values()) {
            // 函数不区分大小写
            if (function.code.equalsIgnoreCase(code)) {
                return function;
            }
        }
        return null;
    }


}
