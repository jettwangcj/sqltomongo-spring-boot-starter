package com.rrtv.parser.data;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

@Data
@Builder
public class SortData implements Serializable {

    /**
     *  排序
     */
    private Sort.Direction direction;

    /**
     *  排序字段
     */
    private String field;


    public SortData(){}

    public SortData(Sort.Direction direction, String field){}
}
