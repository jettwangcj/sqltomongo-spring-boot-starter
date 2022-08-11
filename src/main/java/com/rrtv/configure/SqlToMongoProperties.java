package com.rrtv.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Classname SqlToMongoProperties
 * @Description
 * @Date 2022/8/9 17:35
 * @Created by wangchangjiu
 */

@Data
@ConfigurationProperties(prefix = "sql-to-mongo")
public class SqlToMongoProperties {

    private String packageSearchPath;

    private boolean cacheEnabled;


}
