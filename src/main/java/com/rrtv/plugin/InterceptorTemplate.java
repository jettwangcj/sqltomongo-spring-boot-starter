package com.rrtv.plugin;

import com.rrtv.analyzer.Analyzer;
import com.rrtv.annotation.Intercepts;
import com.rrtv.annotation.Signature;
import com.rrtv.executor.Executor;
import com.rrtv.parser.*;
import com.rrtv.parser.data.PartSQLParserData;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.List;

/**
 * @Classname DefaultInterceptor
 * @Description 拦截器模板，自定义拦截器可以继承这个模板
 * @Date 2022/8/12 16:39
 * @Created by wangchangjiu
 */
@Slf4j
@Intercepts(
    {
      @Signature(type = PartSQLParser.class, method = "proceedData", args = {PlainSelect.class, PartSQLParserData.class}),
      @Signature(type = Executor.class, method = "selectOne", args = {String.class, Class.class, Object[].class}),
      @Signature(type = Executor.class, method = "selectList", args = {String.class, Class.class, Object[].class}),
      @Signature(type = Analyzer.class, method = "proceed", args = {List.class, PartSQLParserData.class})
    }
)
public class InterceptorTemplate implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        if(target instanceof Executor){
            return this.interceptExecutor(invocation);
        } else if(target instanceof Analyzer) {

            log.info("----进入 Analyzer 分析器方法--- ");

        } else {
            PlainSelect plain = (PlainSelect) args[0];
            PartSQLParserData data = (PartSQLParserData) args[1];
            if(target instanceof HavingSQLParser){
                return this.interceptHavingSQLParser(plain, data, invocation);
            } else if(target instanceof GroupSQLParser) {
                return this.interceptGroupSQLParser(plain, data, invocation);
            } else if(target instanceof JoinSQLParser) {
                return this.interceptJoinSQLParser(plain, data, invocation);
            } else if(target instanceof LimitSQLParser) {
                return this.interceptLimitSQLParser(plain, data, invocation);
            } else if(target instanceof OrderSQLParser) {
                return this.interceptOrderSQLParser(plain, data, invocation);
            } else if(target instanceof ProjectSQLParser) {
                return this.interceptProjectSQLParser(plain, data, invocation);
            } else if(target instanceof WhereSQLParser) {
                return this.interceptWhereSQLParser(plain, data, invocation);
            }
        }
        return invocation.proceed();
    }

    private Object interceptExecutor(Invocation invocation) throws Exception {
        return invocation.proceed();
    }


    public Object interceptHavingSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptGroupSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptLimitSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptOrderSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptProjectSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptWhereSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }

    public Object interceptJoinSQLParser(PlainSelect plain, PartSQLParserData data, Invocation invocation) throws Exception {
        return invocation.proceed();
    }
}
