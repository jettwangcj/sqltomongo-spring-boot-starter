package com.rrtv.binding;

import com.rrtv.orm.Configuration;
import com.rrtv.orm.XNode;
import com.rrtv.util.SqlCommonUtil;
import com.rrtv.util.SqlSupportedSyntaxCheckUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname MapperAnnotationBuilder
 * @Description
 * @Date 2022/8/10 17:06
 * @Created by wangchangjiu
 */
public class MapperAnnotationBuilder {

    private final Configuration configuration;
    private final Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        this.configuration = configuration;
        this.type = type;
    }

    public void parse() {
        Method[] methods = ReflectionUtils.getDeclaredMethods(type);
        if(methods != null && methods.length > 0){
            Stream.of(methods).forEach(method -> {
                Select select = method.getAnnotation(Select.class);
                if(select != null){
                    String sql = select.value();

                    SqlSupportedSyntaxCheckUtil.checkSelectSql(sql);

                    String key = type.getName() + "." + method.getName();
                    XNode xNode = new XNode();
                    xNode.setId(method.getName());
                    xNode.setNamespace(type.getName());
                    xNode.setSql(sql);
                    xNode.setSqlType(SqlCommonUtil.SqlType.SELECT);
                    configuration.addMapperElement(key, xNode);
                }
            });
        }
    }

}
