package com.rrtv.orm;

import com.rrtv.util.SqlCommonUtil;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomParser {

    // 获取SQL语句信息
    public static Map<String, XNode> parser(List<Element> list) {
        Map<String, XNode> map = new HashMap<>();
        for (Element root : list) {
            //命名空间
            String namespace = root.attributeValue("namespace");
            // SELECT
            List<Element> selectNodes = root.selectNodes("select");
            for (Element node : selectNodes) {
                String id = node.attributeValue("id");
                String sql = node.getText();
                XNode xNode = new XNode();
                xNode.setNamespace(namespace);
                xNode.setId(id);
                xNode.setSql(sql);
                xNode.setSqlType(SqlCommonUtil.SqlType.SELECT);
                map.put(namespace + "." + id, xNode);
            }
        }
        return map;
    }

}
