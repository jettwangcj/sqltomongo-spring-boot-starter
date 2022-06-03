package com.rrtv.orm;

import com.rrtv.util.SqlCommonUtil;
import lombok.Data;

@Data
public class XNode {

    private SqlCommonUtil.SqlType sqlType;
    private String namespace;
    private String id;
    private String sql;

}
