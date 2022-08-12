package com.rrtv.parser;

import com.rrtv.adapter.MatchExpressionVisitorAdapter;
import com.rrtv.common.ParserPartTypeEnum;
import com.rrtv.parser.data.MatchData;
import com.rrtv.parser.data.PartSQLParserData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HavingSQLParser implements PartSQLParser {

    public List<MatchData> parser(Expression having) {
        if (ObjectUtils.isNotEmpty(having)) {
            MatchExpressionVisitorAdapter adapter = new MatchExpressionVisitorAdapter(ParserPartTypeEnum.HAVING);
            having.accept(adapter);
            List<MatchData> items = adapter.getItems();
            items = items.stream()
                    .sorted(Comparator.comparing(MatchData::getPriority).reversed().thenComparing(MatchData::getSort))
                    .collect(Collectors.toList());
            return items;
        }
        return new ArrayList<>();
    }


    @Override
    public void proceedData(PlainSelect plain, PartSQLParserData data) {
        List<MatchData> matchData = this.parser(plain.getHaving());
        data.setHavingData(matchData);
    }
}
