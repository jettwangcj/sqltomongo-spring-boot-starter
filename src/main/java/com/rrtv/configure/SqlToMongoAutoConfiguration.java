package com.rrtv.configure;

import com.rrtv.SQLToMongoTemplate;
import com.rrtv.cache.MongoTemplateProxy;
import com.rrtv.orm.SqlSession;
import com.rrtv.orm.SqlSessionBuilder;
import com.rrtv.plugin.InterceptorConfigurer;
import com.rrtv.plugin.InterceptorConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties({ SqlToMongoProperties.class })
public class SqlToMongoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InterceptorConfigurer interceptorConfigurer(){
        return new InterceptorConfigurerAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn({"mongoTemplate"})
    public SqlSession sqlSession(@Autowired(required = false) MongoTemplateProxy mongoTemplate,
                                 @Autowired  SqlToMongoProperties properties,
                                 @Autowired InterceptorConfigurer interceptorConfigurer) throws Exception {
        return new SqlSessionBuilder().build(mongoTemplate, interceptorConfigurer,  properties);
    }


    @Bean
    @ConditionalOnMissingBean
    public SQLToMongoTemplate sqlToMongoTemplate(@Autowired SqlSession sqlSession){
        return new SQLToMongoTemplate(sqlSession);
    }

}
