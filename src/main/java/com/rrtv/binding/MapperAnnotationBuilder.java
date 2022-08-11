package com.rrtv.binding;

import com.rrtv.orm.Configuration;

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

    /*private static final Set<Class<? extends Annotation>> statementAnnotationTypes = Stream
            .of(Select.class, Update.class, Insert.class, Delete.class, SelectProvider.class, UpdateProvider.class,
                    InsertProvider.class, DeleteProvider.class)
            .collect(Collectors.toSet());
*/
    private final Configuration configuration;
    private final Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        this.configuration = configuration;
        this.type = type;
    }

    public void parse() {
        String resource = type.toString();
        /*if (!configuration.isResourceLoaded(resource)) {
            loadXmlResource();
            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            parseCache();
            parseCacheRef();
            for (Method method : type.getMethods()) {
                if (!canHaveStatement(method)) {
                    continue;
                }
                if (getAnnotationWrapper(method, false, Select.class, SelectProvider.class).isPresent()
                        && method.getAnnotation(ResultMap.class) == null) {
                    parseResultMap(method);
                }
                try {
                    parseStatement(method);
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteMethod(new MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();*/
    }

}
