package com.rrtv.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Classname InterceptorChain
 * @Description 拦截器链 责任链设计模式
 * @Date 2022/8/11 18:13
 * @Created by wangchangjiu
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            // 多重嵌套 责任链一层套一层
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

}
