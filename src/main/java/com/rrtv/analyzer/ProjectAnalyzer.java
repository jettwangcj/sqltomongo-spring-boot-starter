package com.rrtv.analyzer;

import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.parser.data.ProjectData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname ProjectAnalyzer
 * @Description 投影分析器
 * @Date 2022/8/12 11:54
 * @Created by wangchangjiu
 */
public class ProjectAnalyzer extends AbstractAnalyzer {

    @Override
    public void proceed(List<AggregationOperation> operations, PartSQLParserData data) {

        String majorTableAlias = data.getMajorTableAlias();
        List<ProjectData> projectData = data.getProjectData();
        List<LookUpData> joinParser = data.getJoinParser();

        // 别名和表的映射
        Map<String, LookUpData> lookUpDataMap = joinParser.stream().collect(Collectors.toMap(LookUpData::getAlias, Function.identity()));

        // 分析投影  构建 mongo API
        operations.addAll(analysisProject(majorTableAlias, projectData, lookUpDataMap));

    }


    /**
     * 分析 投影 构建 投影 Mongo API
     *
     * @param majorTableAlias
     * @param projectData
     * @param lookUpDataMap
     * @return
     */
    private static List<AggregationOperation> analysisProject(String majorTableAlias, List<ProjectData> projectData,
                                                              Map<String, LookUpData> lookUpDataMap) {
        List<AggregationOperation> operations = new ArrayList<>();
        List<Field> fields = projectData.stream().map(project -> {
            // 字段携带表别名 例如 select t1.id,t2.name from
            if (StringUtils.isNotEmpty(project.getTable())) {
                String table = project.getTable();
                if (table.equals(majorTableAlias)) {
                    // 主表投影
                    return Fields.field(project.getAlias(), project.getField());
                }

                // 被关联表 需要携带 as （被关联表数据集）
                LookUpData lookUpData = lookUpDataMap.get(project.getTable());
                return Fields.field(project.getField(), lookUpData.getAs().concat(".").concat(project.getField()));
            }

            // 没有 字段携带的 就是单表
            return Fields.field(project.getField(), project.getAlias());
        }).collect(Collectors.toList());
        operations.add(Aggregation.project(Fields.from(fields.toArray(new Field[fields.size()]))));
        return operations;
    }

}
