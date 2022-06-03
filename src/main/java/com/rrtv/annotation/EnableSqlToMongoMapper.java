package com.rrtv.annotation;

import com.rrtv.configure.SqlToMongoRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SqlToMongoRegistrar.class)
public @interface EnableSqlToMongoMapper {

    /**
     * 要扫描的接口类的包名
     *
     * @return
     */
    String[] basePackages() default {};

}
