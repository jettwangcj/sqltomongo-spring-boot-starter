package com.rrtv.parser.data;

import com.rrtv.common.AggregationFunction;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 *  投影数据
 */
@Data
@Builder
public class ProjectData {

    /**
     *  投影的是哪个表
     */
    private String table;
    /**
     * 字段
     */
    private String field;

    /**
     *  显示别名
     */
    private String alias;

    /**
     *  使用哪个聚合函数
     */
    private AggregationFunction function;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectData that = (ProjectData) o;
        return Objects.equals(table, that.table) && Objects.equals(field, that.field) && function == that.function;
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, field, function);
    }

}
