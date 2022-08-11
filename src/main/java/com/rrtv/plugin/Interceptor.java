package com.rrtv.plugin;

import java.util.Properties;

/**
 * @Classname Interceptor
 * @Description 拦截器
 * @Date 2022/8/11 18:08
 * @Created by wangchangjiu
 */
public interface Interceptor {

    Object intercept(Invocation invocation) throws Throwable;

    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
        // NOP
    }

}
