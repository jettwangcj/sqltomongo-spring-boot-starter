package com.rrtv.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Classname SqlToMongoProperties
 * @Description
 * @Date 2022/8/9 17:35
 * @Created by wangchangjiu
 */

@Data
@Component
@ConfigurationProperties(prefix = "sql-to-mongo")
public class SqlToMongoProperties {



}
