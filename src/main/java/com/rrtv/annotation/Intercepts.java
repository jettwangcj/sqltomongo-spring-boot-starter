package com.rrtv.annotation;

import java.lang.annotation.*;

/**
 * @Classname Intercepts
 * @Description
 * @Date 2022/8/12 16:28
 * @Created by wangchangjiu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
    /**
     * Returns method signatures to intercept.
     *
     * @return method signatures
     */
    Signature[] value();
}

