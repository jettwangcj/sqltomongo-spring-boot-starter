package com.rrtv.orm;

import com.rrtv.SQLToMongoTemplate;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SqlSessionBuilder {

    public DefaultSqlSession build(SQLToMongoTemplate sqlToMongoTemplate, String packageSearchPath) throws Exception {
        // 读取配置
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        List<Element> list = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            // Dom4j 解析
            Document document = new SAXReader().read(new InputSource(new InputStreamReader(resource.getInputStream())));
            list.add(document.getRootElement());
        }
        return new DefaultSqlSession(sqlToMongoTemplate, DomParser.parser(list));
    }

}
