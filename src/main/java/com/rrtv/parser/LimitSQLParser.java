package com.rrtv.parser;

import com.rrtv.parser.data.LimitData;
import com.rrtv.parser.data.MatchData;
import com.rrtv.parser.data.PartSQLParserData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Objects;

public class LimitSQLParser implements PartSQLParser {

    private static final Log logger = LogFactory.getLog(LimitSQLParser.class);

    public LimitData parser(Limit limit) {
        if (Objects.nonNull(limit)) {
            Expression offset = limit.getOffset();
            Expression rowCount = limit.getRowCount();
            if (offset instanceof JdbcNamedParameter && rowCount instanceof JdbcNamedParameter) {
                String offsetValue = JdbcNamedParameter.class.cast(offset).getName();
                String rowCountValue = JdbcNamedParameter.class.cast(rowCount).getName();
                return new LimitData(Integer.valueOf(offsetValue), Integer.valueOf(rowCountValue));
            } else {
                if(logger.isWarnEnabled()){
                    logger.warn("offset 或者 rowCount 不是 JdbcNamedParameter 实例，放弃解析 Limit");
                }
            }
        }
        return null;
    }

    @Override
    public void proceedData(PlainSelect plain, PartSQLParserData data) {
        LimitData limitData = this.parser(plain.getLimit());
        data.setLimitData(limitData);
    }
}
