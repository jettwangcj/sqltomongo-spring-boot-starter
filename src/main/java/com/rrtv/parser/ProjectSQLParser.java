package com.rrtv.parser;

import com.rrtv.common.AggregationFunction;
import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.parser.data.ProjectData;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectSQLParser implements PartSQLParser{

    public List<ProjectData> parser(List<SelectItem> selectItems) {

        List<ProjectData> projects = new ArrayList<>();
        for (SelectItem selectItem : selectItems) {
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            // 别名
            String alias = selectExpressionItem.getAlias() == null ? "" : selectExpressionItem.getAlias().getName();

            Expression expression = selectExpressionItem.getExpression();
            //判断表达式是否是函数

            String functionName = null;
            if (expression instanceof Function) {
                Function function = (Function) expression;
                functionName = function.getName();
                SqlSupportedSyntaxCheckUtil.checkProjectSupportFunction(functionName);
                ExpressionList parameters = function.getParameters();
                List<Expression> expressions = parameters.getExpressions();

                SqlSupportedSyntaxCheckUtil.checkFunctionColumn(expressions);

                // 解析出 函数 和 字段
                expression = expressions.get(0);
            }


            if (expression instanceof Column) {

                Column column = Column.class.cast(expression);

                String columnName = column.getColumnName();
                alias = StringUtils.isNotBlank(alias) ? alias : columnName;

                String table = null;
                if(ObjectUtils.isNotEmpty(column.getTable())){
                    table = column.getTable().getName();
                }

                projects.add(ProjectData.builder().alias(alias).table(table).field(columnName)
                        .function(AggregationFunction.parser(functionName)).build());
            }


        }
        return projects;
    }

    @Override
    public void proceedData(PlainSelect plain, PartSQLParserData data) {
        List<ProjectData> projectData = this.parser(plain.getSelectItems());
        data.setProjectData(projectData);
    }
}
