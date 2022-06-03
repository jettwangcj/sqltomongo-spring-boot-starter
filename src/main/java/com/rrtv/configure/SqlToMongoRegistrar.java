package com.rrtv.configure;

import com.rrtv.annotation.EnableSqlToMongoMapper;
import com.rrtv.annotation.SqlToMongoMapper;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlToMongoRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    SqlToMongoRegistrar() {
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // true：默认TypeFilter生效，这种模式会查询出许多不符合你要求的class名
        // false：关闭默认TypeFilter
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent() && !beanDefinition.getMetadata().isAnnotation();
            }
        };
        scanner.setResourceLoader(this.resourceLoader);
        // 添加一个注解过滤器，有 SqlToMongoMapper 注解的类/接口才继续处理
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(SqlToMongoMapper.class);
        scanner.addIncludeFilter(annotationTypeFilter);

        // 这里的metadata是spring启动类上的注解元数据，下面这一步是获取 EnableSqlToMongoMapper 注解的属性
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableSqlToMongoMapper.class.getName());

        // 得到 EnableSqlToMongoMapper 注解上的basePackages属性值，只扫描这些包下的class
        Set<String> basePackages = new HashSet<>();

        String[] packages = (String[]) attributes.get("basePackages");
        for(String packageItem : packages){
            if(!StringUtils.isEmpty(packageItem)){
                basePackages.add(packageItem);
            }
        }

        // 遍历要扫描的包
        for (String basePackage : basePackages) {
            // 扫描指定的包路径，获取相应的BeanDefinition。扫描后的类可以通过过滤器进行排除
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    // 扫描到的接口/类的注解元数据
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    // 注册
                    registerSqlToMongoMapper(registry, annotationMetadata);
                }
            }
        }
    }


    private void registerSqlToMongoMapper(BeanDefinitionRegistry registry,
                                          AnnotationMetadata annotationMetadata) {
        // 这个类就是扫描到的要处理的类
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(SqlToMongoMapperFactoryBean.class);

        // 通过此方式给 SqlToMongoMapperFactoryBean 的成员赋值，将要实现的类传入
        definition.addPropertyValue("mapperInterface", className);
        // 设置注入方式
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

        // 通过 SqlToMongoMapperFactoryBean 生成指定类的实现，这个类就可以通过@Autowired注入了
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
