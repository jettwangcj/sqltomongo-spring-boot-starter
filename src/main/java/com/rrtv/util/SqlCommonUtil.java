package com.rrtv.util;

import com.rrtv.exception.SqlParserException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.io.StringReader;

public class SqlCommonUtil {

    public enum SqlType {
        ALTER,
        CREATEINDEX,
        CREATETABLE,
        CREATEVIEW,
        DELETE,
        DROP,
        EXECUTE,
        INSERT,
        MERGE,
        REPLACE,
        SELECT,
        TRUNCATE,
        UPDATE,
        UPSERT,
        NONE

    }

    public static SqlType getSqlType(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        if (sqlStmt instanceof Alter) {
            return SqlType.ALTER;
        } else if (sqlStmt instanceof CreateIndex) {
            return SqlType.CREATEINDEX;
        } else if (sqlStmt instanceof CreateTable) {
            return SqlType.CREATETABLE;
        } else if (sqlStmt instanceof CreateView) {
            return SqlType.CREATEVIEW;
        } else if (sqlStmt instanceof Delete) {
            return SqlType.DELETE;
        } else if (sqlStmt instanceof Drop) {
            return SqlType.DROP;
        } else if (sqlStmt instanceof Execute) {
            return SqlType.EXECUTE;
        } else if (sqlStmt instanceof Insert) {
            return SqlType.INSERT;
        } else if (sqlStmt instanceof Merge) {
            return SqlType.MERGE;
        } else if (sqlStmt instanceof Replace) {
            return SqlType.REPLACE;
        } else if (sqlStmt instanceof Select) {
            return SqlType.SELECT;
        } else if (sqlStmt instanceof Truncate) {
            return SqlType.TRUNCATE;
        } else if (sqlStmt instanceof Update) {
            return SqlType.UPDATE;
        } else if (sqlStmt instanceof Upsert) {
            return SqlType.UPSERT;
        } else {
            return SqlType.NONE;
        }
    }

    /**
     *  初步解析 select SQL
     * @param sql
     * @return
     */
    public static PlainSelect parserSelectSql(String sql) {

        SqlSupportedSyntaxCheckUtil.checkSqlType(sql, SqlType.SELECT);

        Select select;
        try {
            select = (Select) CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException ex) {
            throw new SqlParserException("sql解析失败：", ex);
        }

        PlainSelect plain = (PlainSelect) select.getSelectBody();
        return plain;
    }

    /**
     * 处理表达式的 右边值
     *
     * @param expression
     * @return
     */
    public static Object handleExpressionValue(Expression expression) {

        Object resValue = expression;
        if (expression instanceof LongValue) {
            LongValue longValue = (LongValue) expression;
            resValue = longValue.getValue();
        } else if (expression instanceof DoubleValue) {
            DoubleValue doubleValue = (DoubleValue) expression;
            resValue = doubleValue.getValue();
        } else if (expression instanceof StringValue) {
            StringValue stringValue = (StringValue) expression;
            resValue = stringValue.getValue();
        } else if (expression instanceof TimestampValue) {
            TimestampValue timestampValue = (TimestampValue) expression;
            resValue = timestampValue.getValue();
        } else if (expression instanceof TimeValue) {
            TimeValue timeValue = (TimeValue) expression;
            resValue = timeValue.getValue();
        } else if (expression instanceof NullValue) {
            resValue = null;
        }
        return resValue;
    }


}
