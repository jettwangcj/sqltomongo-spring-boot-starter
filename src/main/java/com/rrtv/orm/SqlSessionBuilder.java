package com.rrtv.orm;

import com.rrtv.configure.SqlToMongoProperties;
import com.rrtv.parser.SelectSQLTypeParser;
import com.rrtv.plugin.InterceptorConfigurer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SqlSessionBuilder {

    public DefaultSqlSession build(MongoTemplate mongoTemplate, InterceptorConfigurer interceptorConfigurer, SqlToMongoProperties properties) throws Exception {
        // 读取配置
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(properties.getPackageSearchPath());
        List<Element> list = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            // Dom4j 解析
            Document document = new SAXReader().read(new InputSource(new InputStreamReader(resource.getInputStream())));
            list.add(document.getRootElement());
        }

        // 构建配置核心对象
        Configuration configuration = new Configuration();
        configuration.setMapperElement(DomParser.parser(list));

        // 添加拦截器
        interceptorConfigurer.addInterceptor(configuration);

        return new DefaultSqlSession(configuration, configuration.newExecutor(mongoTemplate, new SelectSQLTypeParser(configuration)));
    }

}
