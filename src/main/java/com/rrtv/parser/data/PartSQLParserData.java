package com.rrtv.parser.data;

import lombok.Data;

import java.util.List;

/**
 * @Classname PartSQLParserData
 * @Description
 * @Date 2022/8/12 15:49
 * @Created by wangchangjiu
 */
@Data
public class PartSQLParserData {

    private String majorTableAlias;

    private String majorTable;

    private List<LookUpData> joinParser;

    private List<ProjectData> projectData;

    private List<MatchData> matchData;

    private List<GroupData> groupData;

    private List<MatchData> havingData;

    private List<SortData> sortData;

    private LimitData limitData;

}
