package com.rrtv.plugin;

import com.rrtv.orm.Configuration;

/**
 * @Classname InterceptorConfigurer
 * @Description
 * @Date 2022/8/11 21:07
 * @Created by wangchangjiu
 */
public interface InterceptorConfigurer {

    void addInterceptor(Configuration configuration);

}
