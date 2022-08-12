package com.rrtv.parser;

import com.rrtv.adapter.MatchExpressionVisitorAdapter;
import com.rrtv.parser.data.MatchData;
import net.sf.jsqlparser.expression.Expression;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HavingSQLParser {

    public List<MatchData> parser(Expression having) {
        if (ObjectUtils.isNotEmpty(having)) {
            MatchExpressionVisitorAdapter adapter = new MatchExpressionVisitorAdapter(MatchExpressionVisitorAdapter.ParserPart.having);
            having.accept(adapter);
            List<MatchData> items = adapter.getItems();
            items = items.stream()
                    .sorted(Comparator.comparing(MatchData::getPriority).reversed().thenComparing(MatchData::getSort))
                    .collect(Collectors.toList());
            return items;
        }
        return new ArrayList<>();
    }




}
