package com.rrtv.orm;

import com.rrtv.cache.CacheManager;
import com.rrtv.cache.DefaultCacheManager;
import com.rrtv.configure.SqlToMongoProperties;
import com.rrtv.plugin.InterceptorConfigurer;
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

/**
 * @Classname ConfigurationBuilder
 * @Description
 * @Date 2022/8/24 21:00
 * @Created by wangchangjiu
 */
public class ConfigurationBuilder {

    public Configuration build(InterceptorConfigurer interceptorConfigurer, SqlToMongoProperties properties) throws Exception {

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

        // 解析 Mapper.xml文件 解析出结果保存  Map<String, XNode> 中
        configuration.setMapperElement(DomParser.parser(list));

        // 添加拦截器
        interceptorConfigurer.addInterceptor(configuration);

        // 设置是否开启缓存
        configuration.setCacheEnabled(properties.isCacheEnabled());

        // 设置缓存管理器
        if(configuration.isCacheEnabled()){
            CacheManager cacheManager = new DefaultCacheManager();
            configuration.setCacheManager(cacheManager);
            configuration.setCache(cacheManager.createCache(properties.getCacheFullName()));
        }
        return configuration;
    }


}
