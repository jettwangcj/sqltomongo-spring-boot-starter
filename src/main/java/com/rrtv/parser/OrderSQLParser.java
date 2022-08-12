package com.rrtv.parser;

import com.rrtv.parser.data.PartSQLParserData;
import com.rrtv.parser.data.SortData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSQLParser implements PartSQLParser{

    public List<SortData> parser(List<OrderByElement> orderByElements) {
        List<SortData> sortData = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderByElements)) {
            for (OrderByElement orderByElement : orderByElements) {
                Expression expression = orderByElement.getExpression();
                if (expression instanceof Column) {
                    Column column = (Column) (expression);
                    Sort.Direction direction =  orderByElement.isAsc() ?  Sort.Direction.ASC : Sort.Direction.DESC;
                    sortData.add(new SortData(direction, column.getColumnName()));
                }
            }
            return sortData;
        }
        return null;
    }

    @Override
    public void proceedData(PlainSelect plain, PartSQLParserData data) {
        List<SortData> sortData = this.parser(plain.getOrderByElements());
        data.setSortData(sortData);
    }
}
