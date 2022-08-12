package com.rrtv.parser;

import com.rrtv.parser.data.PartSQLParserData;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * @Classname PartSQLParser
 * @Description
 * @Date 2022/8/12 15:26
 * @Created by wangchangjiu
 */
public interface PartSQLParser {

    void proceedData(PlainSelect plain, PartSQLParserData data);

}
