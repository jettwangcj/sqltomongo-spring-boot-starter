package com.rrtv.adapter;

import com.rrtv.common.AggregationFunction;
import com.rrtv.common.ParserPartTypeEnum;
import com.rrtv.exception.NotSupportFunctionException;
import com.rrtv.parser.data.MatchData;
import com.rrtv.util.SqlCommonUtil;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;

public class MatchExpressionVisitorAdapter extends ExpressionVisitorAdapter {

    private List<MatchData> items = new ArrayList<>();

    private int priority = 0;

    private ParserPartTypeEnum part;

    public List<MatchData> getItems() {
        return items;
    }

    public MatchExpressionVisitorAdapter() {}

    public MatchExpressionVisitorAdapter(ParserPartTypeEnum part){
        this.part = part;
    }

    public void processLogicalExpression(BinaryExpression expr, String logic) {
        MatchData.OperatorExpressionItem item = new MatchData.OperatorExpressionItem(logic);
        items.add(new MatchData(priority, 0, true, item));

        priority++;
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
        if (priority != 0) {
            priority--;
        }
    }

    @Override
    protected void visitBinaryExpression(BinaryExpression expr) {
        if (expr instanceof ComparisonOperator || expr instanceof LikeExpression) {

            if (!(expr.getLeftExpression() instanceof Column) && ParserPartTypeEnum.WHERE == part) {
                //报错 暂不支持 where 不支持 函数
                throw new NotSupportFunctionException("where 条件不支持函数操作");
            }

            Expression expression = expr.getLeftExpression();
            AggregationFunction function = null;
            if(expr.getLeftExpression() instanceof Function && ParserPartTypeEnum.HAVING == part){
                Function leftFunction = (Function) expr.getLeftExpression();
                function = AggregationFunction.parser(leftFunction.getName());
                ExpressionList parameters = leftFunction.getParameters();
                List<Expression> expressions = parameters.getExpressions();
                SqlSupportedSyntaxCheckUtil.checkFunctionColumn(expressions);
                // 解析出 函数 和 字段
                expression = expressions.get(0);
            }

            if( expression instanceof Column){
                Column leftColumn = Column.class.cast(expression);
                String tableAlias = leftColumn.getTable() == null ? null : leftColumn.getTable().getName();
                MatchData.RelationExpressionItem item = new MatchData.RelationExpressionItem(tableAlias, leftColumn.getColumnName(),
                        expr.getStringExpression(), function, SqlCommonUtil.handleExpressionValue(expr.getRightExpression()));
                items.add(new MatchData(priority, 1, false, item));
            }
        }
        super.visitBinaryExpression(expr);
    }

    @Override
    public void visit(AndExpression expr) {
        processLogicalExpression(expr, "AND");

    }

    @Override
    public void visit(OrExpression expr) {
        processLogicalExpression(expr, "OR");
    }

    @Override
    public void visit(InExpression expr) {

        if (!(expr.getLeftExpression() instanceof Column)) {
            //报错 暂不支持
            //  throw new RuntimeException()
        }

        ItemsList rightItemsList = expr.getRightItemsList();
        if (rightItemsList instanceof ExpressionList) {

            List<Object> valueList = new ArrayList<>();
            ExpressionList expressionList = (ExpressionList) rightItemsList;
            List<Expression> expressions = expressionList.getExpressions();
            expressions.stream().forEach(expression -> valueList.add(SqlCommonUtil.handleExpressionValue(expression)));

            Column leftColumn = Column.class.cast(expr.getLeftExpression());
            String tableAlias = leftColumn.getTable() == null ? null : leftColumn.getTable().getName();
            MatchData.RelationExpressionItem item = new MatchData.RelationExpressionItem(tableAlias, leftColumn.getColumnName(),
                    "IN", null, valueList);
            items.add(new MatchData(priority, 1, false, item));
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }



}
