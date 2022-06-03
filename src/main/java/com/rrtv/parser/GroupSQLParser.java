package com.rrtv.parser;

import com.rrtv.parser.data.GroupData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupSQLParser {

    public static void main(String[] args) {
        try {

            String sql = "select b.title, b.remark from bookList b group by b.id,c.id";
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect plain = (PlainSelect) select.getSelectBody();
            parser(plain.getGroupBy());

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static List<GroupData> parser(GroupByElement element) {
        List<GroupData> groupData = new ArrayList<>();
        if ( Objects.nonNull(element)) {
            List<Expression> groupByExpressions = element.getGroupByExpressions();
            for (Expression groupByExpression : groupByExpressions) {
                if (groupByExpression instanceof Column) {
                    Column column = (Column) (groupByExpression);
                    // 分组字段的表别名
                    String tableAlias = column.getTable() == null ? "" : column.getTable().getName();
                    groupData.add(new GroupData(tableAlias, column.getColumnName()));
                }
            }
        }
        return groupData;
    }
}
