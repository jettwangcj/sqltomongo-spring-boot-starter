package com.rrtv.parser.data;

import com.rrtv.common.ConversionFunction;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LookUpData implements Serializable {

    /** 要连接的目标表 **/
    private String table;
    /**
     *  连接表别名
     */
    private String alias;
    /**
     *  作为连接参照的本表字段
     */
    private String localField;
    /**
     *  作为连接参照的目标表字段
     */
    private String foreignField;
    /**
     *  当连接查询查询出来之后，外表的查询结果
     */
    private String as;
    /**
     *  转换函数 连接条件需要转化
     */
    private ConversionFunction function;
    /**
     *  发生函数转换时在作用在哪张表
     */
    private String conversionFieldTable;


}
