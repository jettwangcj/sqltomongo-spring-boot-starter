package com.rrtv.analyzer;

import com.rrtv.parser.data.*;
import com.rrtv.plugin.Interceptor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname AnalyzerChain
 * @Description
 * @Date 2022/8/12 14:53
 * @Created by wangchangjiu
 */
public class AnalyzerChain {

    private final List<Analyzer> interceptors = new ArrayList<>();

    private PartSQLParserData partSQLParserData;


    public AnalyzerChain(List<LookUpData> joinParser, List<ProjectData> projectData,
                         List<MatchData> matchData, List<GroupData> groupData,
                         List<MatchData> havingData, List<SortData> sortData, LimitData limitData){
        this.partSQLParserData = PartSQLParserData.builder()
                .build();
    }


   public void addAnalyzer(Analyzer analyzer){
       interceptors.add(analyzer);
   }





    @Setter
    @Builder
    @NoArgsConstructor
    private class PartSQLParserData {

        List<LookUpData> joinParser;

        List<ProjectData> projectData;

        List<MatchData> matchData;

        List<GroupData> groupData;

        List<MatchData> havingData;

        List<SortData> sortData;

        List<LimitData> limitData;

    }

}
