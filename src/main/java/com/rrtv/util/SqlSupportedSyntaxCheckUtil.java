package com.rrtv.util;

import com.rrtv.common.AggregationFunction;
import com.rrtv.common.ConversionFunction;
import com.rrtv.exception.NotSupportFunctionException;
import com.rrtv.exception.SqlParserException;
import com.rrtv.exception.SqlTypeException;
import com.rrtv.exception.TableAssociationException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class SqlSupportedSyntaxCheckUtil {

    public static void checkSqlType(String sql, SqlCommonUtil.SqlType sqlType){
        SqlCommonUtil.SqlType type;
        try {
            type = SqlCommonUtil.getSqlType(sql);
        } catch (JSQLParserException exception) {
            throw new SqlParserException("不能获取SQL类型，解析SQL失败：", exception.getCause());
        }

        if(sqlType != type){
            throw new SqlTypeException("SQL语句和SQL类型不匹配");
        }

    }

    /**
     *  检查 投影支持的函数
     * @param functionName
     */
    public static void checkProjectSupportFunction(String functionName){
        AggregationFunction aggregationFunction = AggregationFunction.parser(functionName);
        if(ObjectUtils.isEmpty(aggregationFunction)){
            throw new NotSupportFunctionException("不支持的函数异常");
        }
    }

    /**
     *  检查是否有子查询 暂时不支持 子查询
     * @param rightItem
     */
    public static void checkSubSelect(FromItem rightItem){

    }

    /**
     *  检查 函数不可以嵌套
     * @param expressions
     */
    public static void checkFunctionColumn(List<Expression> expressions) {
        if(CollectionUtils.isEmpty(expressions) || expressions.size() > 1){
            // 错误
            throw new NotSupportFunctionException("不支持嵌套函数异常");
        }
        if(expressions.get(0) instanceof Column){
            throw new SqlParserException("SQL 语法错误，函数里面的参数应该是字段的列");
        }
    }

    /**
     *  表关联检查
     * @param expressions
     */
    public static void checkTableAssociationCondition(Collection<Expression> expressions) {
        if(CollectionUtils.isEmpty(expressions)){
            throw new TableAssociationException("表关联异常，表关联条件不能为空");
        }

        if(expressions.size() != 1){
            throw new TableAssociationException("表关联条件只能是一个");
        }

        Iterator<Expression> iterator = expressions.iterator();
        while (iterator.hasNext()) {
            Expression expression = iterator.next();
            if(!(expression instanceof EqualsTo)){
                throw new TableAssociationException("表关联只支持等值关联");
            }
        }
    }

    public static void checkTableAssociationSupportedFunction(Function leftFunction) {

        if(ConversionFunction.parser(leftFunction.getName()) == null){
            throw new TableAssociationException("表关联条件使用的函数不支持");
        }

        List<Expression> expressions = leftFunction.getParameters().getExpressions();
        if(CollectionUtils.isEmpty(expressions) || expressions.size() > 1){
            throw new TableAssociationException("表关联函数参数有误");
        }

    }
}
