package com.rrtv.analyzer;

import com.rrtv.parser.data.LookUpData;
import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.parser.data.ProjectData;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Classname JoinAnalyzer
 * @Description Join 分析器
 * @Date 2022/8/12 11:51
 * @Created by wangchangjiu
 */
public class JoinAnalyzer extends AbstractAnalyzer {

    @Override
    public void proceed(List<AggregationOperation> operations,  PartSQLParserData data) {

        List<ProjectData> projectData = data.getProjectData();
        List<LookUpData> joinParser = data.getJoinParser();

        operations.addAll(analysisJoin(joinParser, projectData));

    }


    /**
     * 分析 JOIN 构建 mongo 表关联 API
     *
     * @param joinParser
     * @param projectData
     * @return
     */
    private static List<AggregationOperation> analysisJoin(List<LookUpData> joinParser, List<ProjectData> projectData) {
        List<AggregationOperation> operations = new ArrayList<>();
        if (!CollectionUtils.isEmpty(joinParser)) {

            Map<String, List<ProjectData>> projectMap = projectData.stream().collect(Collectors.groupingBy(ProjectData::getTable));

            joinParser.stream().forEach(join -> {
                // 这里判断是否有携带函数的， 携带函数的，需要把那个表的字段先投影出来 在做 函数转换
                if (ObjectUtils.isNotEmpty(join.getFunction())) {
                    // 关联表 需要转换 例如 from tab1 t1 left join tab2 t2 on t1.id = ObjectId(t2.oid) 那么需要先投影
                    // select c.bookListId, c.lang, b.title from categoryModuleRecommend c left join bookList b on ObjectId(c.bookListId) = b.id
                    // 找出需要转换函数处理的是哪张表，投影出这张表的字段，包括关联字段
                    List<ProjectData> projectDataList = projectMap.get(join.getConversionFieldTable());
                    List<String> fields = Optional.of(projectDataList.stream().map(ProjectData::getField).collect(Collectors.toList())).orElse(new ArrayList<>());
                    // 关联字段不在 select 里面，把它包含进去，这个字段需要投影出来
                    String localField = join.getLocalField();
                    if (!fields.contains(localField)) {
                        fields.add(localField);
                    }
                    // 投影
                    operations.add(Aggregation.project(fields.toArray(new String[fields.size()]))
                            .and(ConvertOperators.Convert.convertValue("$".concat(localField)).to(join.getFunction().getCode())).as(localField));
                }
                // 关联表
                operations.add(Aggregation.lookup(join.getTable(), join.getLocalField(), join.getForeignField(), join.getAs()));
                // 展平
                operations.add(Aggregation.unwind(join.getAs(), true));
            });
        }
        return operations;
    }
}
