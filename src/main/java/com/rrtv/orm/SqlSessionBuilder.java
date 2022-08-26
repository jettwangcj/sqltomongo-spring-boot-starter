package com.rrtv.orm;

import com.rrtv.parser.SelectSQLTypeParser;
import org.springframework.data.mongodb.core.MongoTemplate;


public class SqlSessionBuilder {

    public DefaultSqlSession build(MongoTemplate mongoTemplate, Configuration configuration) {
        return new DefaultSqlSession(configuration, configuration.newExecutor(mongoTemplate, new SelectSQLTypeParser(configuration)));
    }

}
