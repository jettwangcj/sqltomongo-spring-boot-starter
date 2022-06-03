package com.rrtv.parser.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupData implements Serializable {

    /**
     *  分组 表别名
     */
    private String tableAlias;

    /**
     *  分组字段
     */
    private String field;

    public GroupData(){}

    public GroupData(String tableAlias, String field){
        this.tableAlias = tableAlias;
        this.field = field;
    }

}
