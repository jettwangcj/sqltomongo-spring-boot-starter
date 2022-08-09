package com.rrtv.orm;

import lombok.Data;

import java.util.Map;

/**
 * @Classname Configuration
 * @Description 解析 配置
 * @Date 2022/8/9 17:30
 * @Created by wangchangjiu
 */

@Data
public class Configuration {

    private Map<String, XNode> mapperElement;

}
