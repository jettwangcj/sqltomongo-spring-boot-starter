package com.rrtv.parser.data;

import com.rrtv.common.ParserPartTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @Classname PartSQLParserResult
 * @Description
 * @Date 2022/8/12 11:36
 * @Created by wangchangjiu
 */
@Data
public class PartSQLParserResult<T> {

    private ParserPartTypeEnum partType;

    private List<T> parserData;

}
