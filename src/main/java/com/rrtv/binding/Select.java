package com.rrtv.binding;

import java.lang.annotation.*;

/**
 * @Classname Select
 * @Description
 * @Date 2022/8/15 17:20
 * @Created by wangchangjiu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {

    String value() default "";

}
