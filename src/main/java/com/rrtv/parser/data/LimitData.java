package com.rrtv.parser.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class LimitData implements Serializable {

    private Integer offsetValue;

    private Integer rowCount;

    public LimitData(){}

    public LimitData(Integer offsetValue, Integer rowCount){
        this.offsetValue = offsetValue;
        this.rowCount = rowCount;
    }
}
