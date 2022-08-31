package com.rrtv.configure;

import com.rrtv.SQLToMongoTemplate;
import com.rrtv.cache.ClearCacheListener;
import com.rrtv.cache.MongoTemplateProxy;
import com.rrtv.cache.SaveMongoEventListener;
import com.rrtv.orm.ConfigurationBuilder;
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
import org.springframework.data.mongodb.MongoDatabaseFactory;

@Configuration
@EnableConfigurationProperties({ SqlToMongoProperties.class })
public class SqlToMongoAutoConfiguration {



    @Bean
    @ConditionalOnMissingBean
    public MongoTemplateProxy mongoTemplate(@Autowired MongoDatabaseFactory mongoDbFactory,
                                            @Autowired com.rrtv.orm.Configuration configuration){
        return new MongoTemplateProxy(mongoDbFactory, configuration);
    }

    @Bean
    public SaveMongoEventListener saveMongoEventListener(@Autowired com.rrtv.orm.Configuration configuration){
        return new SaveMongoEventListener(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public InterceptorConfigurer interceptorConfigurer(){
        return new InterceptorConfigurerAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public com.rrtv.orm.Configuration configuration(@Autowired SqlToMongoProperties properties,
                                                    @Autowired InterceptorConfigurer interceptorConfigurer) throws Exception {
        return new ConfigurationBuilder().build(interceptorConfigurer, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClearCacheListener clearCacheListener(@Autowired com.rrtv.orm.Configuration configuration){
        return new ClearCacheListener(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn({"mongoTemplate"})
    public SqlSession sqlSession(@Autowired(required = false) MongoTemplateProxy mongoTemplate,
                                 @Autowired com.rrtv.orm.Configuration configuration) {
        return new SqlSessionBuilder().build(mongoTemplate, configuration);
    }


    @Bean
    @ConditionalOnMissingBean
    public SQLToMongoTemplate sqlToMongoTemplate(@Autowired SqlSession sqlSession){
        return new SQLToMongoTemplate(sqlSession);
    }

}
