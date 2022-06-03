package com.rrtv.configure;

import com.rrtv.SQLToMongoTemplate;
import com.rrtv.orm.SqlSession;
import com.rrtv.orm.SqlSessionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SqlToMongoAutoConfiguration {

    @Value("${sql-to-mongo.mapper.base-package:classpath*:mapper/*.xml}")
    private String mapperBasePackage;

    @Bean
    @ConditionalOnMissingBean
  //  @ConditionalOnBean({ MongoTemplate.class })
    @DependsOn("mongoTemplate")
    public SQLToMongoTemplate sqlToMongoTemplate(@Autowired(required = false)
                                                 MongoTemplate mongoTemplate){
        return new SQLToMongoTemplate(mongoTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSession sqlSession(SQLToMongoTemplate mongoTemplate) throws Exception {
        return new SqlSessionBuilder().build(mongoTemplate, mapperBasePackage);
    }

}
