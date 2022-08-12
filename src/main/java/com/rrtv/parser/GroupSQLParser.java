package com.rrtv.parser;

import com.rrtv.parser.data.GroupData;
import com.rrtv.parser.data.MatchData;
import com.rrtv.parser.data.PartSQLParserData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupSQLParser implements PartSQLParser{

    public List<GroupData> parser(GroupByElement element) {
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

    @Override
    public void proceedData(PlainSelect plain, PartSQLParserData data) {
        List<GroupData> groupData = this.parser(plain.getGroupBy());
        data.setGroupData(groupData);
    }
}
