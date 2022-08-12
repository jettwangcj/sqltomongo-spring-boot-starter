package com.rrtv.plugin;

import com.rrtv.parser.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @Classname InterceptorTemplate
 * @Description
 * @Date 2022/8/12 16:39
 * @Created by wangchangjiu
 */
@Slf4j
public class InterceptorTemplate implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object target = invocation.getTarget();
        if(target instanceof HavingSQLParser){

        } else if(target instanceof GroupSQLParser) {
            log.info("-------GroupSQLParser--------------");
        } else if(target instanceof JoinSQLParser) {
            log.info("-------JoinSQLParser--------------");
        } else if(target instanceof LimitSQLParser) {
            log.info("-------LimitSQLParser--------------");
        } else if(target instanceof OrderSQLParser) {
            log.info("-------OrderSQLParser--------------");
        } else if(target instanceof ProjectSQLParser) {
            log.info("-------ProjectSQLParser--------------");
        } else if(target instanceof WhereSQLParser) {
            log.info("-------WhereSQLParser--------------");
        }
        return invocation.proceed();
    }
}
