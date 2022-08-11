package com.rrtv.parser;

import com.rrtv.common.ConversionFunction;
import com.rrtv.parser.data.LookUpData;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class JoinSQLParser {

    public List<LookUpData> parser(List<Join> joins, String majorTableAlias ) {

        List<LookUpData> lookUpData = new ArrayList<>();

        if(!CollectionUtils.isEmpty(joins)){
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                SqlSupportedSyntaxCheckUtil.checkSubSelect(rightItem);
                if (rightItem instanceof Table) {
                    Table table = (Table) (rightItem);

                    // 检查 表关联时 只支持 等值匹配并且匹配条件只有一个
                    SqlSupportedSyntaxCheckUtil.checkTableAssociationCondition(join.getOnExpressions());

                    // 根据 外键关联  所以条件只有一个
                    Expression onExpression = join.getOnExpression();

                    EqualsTo equalsTo = EqualsTo.class.cast(onExpression);

                    Expression leftExpression = equalsTo.getLeftExpression();
                    Expression rightExpression = equalsTo.getRightExpression();

                    ConversionFunction function = null;
                    Column left , right;
                    String conversionFieldTable = null;

                    if(leftExpression instanceof Function){
                        // 检查支持的函数
                        // 因为 JOIN 函数支持 类型转换函数 检查有多个参数报错或者没有参数报错
                       Function leftFunction = Function.class.cast(leftExpression);

                       SqlSupportedSyntaxCheckUtil.checkTableAssociationSupportedFunction(leftFunction);

                       function = ConversionFunction.parser(leftFunction.getName());
                       left = Column.class.cast(leftFunction.getParameters().getExpressions().get(0));
                       conversionFieldTable = left.getTable().getName();
                    } else {
                        left = Column.class.cast(leftExpression);
                    }

                    if(rightExpression instanceof Function){

                        Function rightFunction = Function.class.cast(rightExpression);
                        // 检查支持的函数 因为 JOIN 函数支持 类型转换函数 检查有多个参数报错或者没有参数报错
                        SqlSupportedSyntaxCheckUtil.checkTableAssociationSupportedFunction(rightFunction);

                        function = ConversionFunction.parser(rightFunction.getName());
                        right = Column.class.cast(rightFunction.getParameters().getExpressions().get(0));
                        conversionFieldTable = right.getTable().getName();
                    } else {
                        right = Column.class.cast(rightExpression);
                    }

                    String currTableAlias = table.getAlias() == null ? "" : table.getAlias().getName();
                    String localField = null, foreignField = null ;

                    if(!StringUtils.isEmpty(currTableAlias)){
                        // 关联表
                        String leftTableName = left.getTable().getName();
                        String rightTableName = right.getTable().getName();

                        // 当出现三表关联时 考虑关联第三张表是主表关联的还是第二张表关联的
                        if(leftTableName.contains(majorTableAlias) || rightTableName.contains(majorTableAlias)){
                            // 使用主表关联  localField: 源集合中的match值 ,bookListId, foreignField: 待Join的集合的match值 id
                            // 源集合 就是主表
                            if(currTableAlias.equals(leftTableName)){
                                foreignField = left.getColumnName();
                                localField = right.getColumnName();
                            } else if(currTableAlias.equals(rightTableName)){
                                foreignField = right.getColumnName();
                                localField = left.getColumnName() ;
                            }

                        } else {
                            // 使用中间表关联
                            // 如果被关联表有别名 那么 别名.字段 的 是外键
                            foreignField = left.getColumnName().contains(currTableAlias) ? left.getColumnName() : right.getColumnName();
                            localField = !left.getColumnName().contains(currTableAlias) ? left.getColumnName() : right.getColumnName();
                            // 关联第三张表时 用第二张表去关联 拼接第二张表名称
                            // 例如： from tab1 t1 left on tab2 t2 on t1.id = t2.t1_id left join tab3 t3 on t2.id = t3.t2_id
                            // 这里 tab3 是和 tab2 关联 所以 localField = tab2.id , mongo 是这样的
                            localField = table.getName().concat(".").concat(localField);
                        }
                    } else {
                        // 没有别名 按照默认习惯  主键在前，外键在后
                        localField = left.getColumnName();
                        foreignField = right.getColumnName();
                    }
                    String as = "tmp_".concat(table.getName());

                    lookUpData.add(LookUpData.builder()
                            .table(table.getName()).localField(localField)
                            .foreignField(foreignField).function(function)
                            .conversionFieldTable(conversionFieldTable)
                            .alias(currTableAlias).as(as).build());
                }
            }
        }
        return lookUpData;
    }
}
