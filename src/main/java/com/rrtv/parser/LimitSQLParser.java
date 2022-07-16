package com.rrtv.parser;

import com.rrtv.exception.SqlParserException;
import com.rrtv.parser.data.LimitData;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.Limit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Objects;

public class LimitSQLParser {

    private static final Log logger = LogFactory.getLog(LimitSQLParser.class);

    public static LimitData parser(Limit limit) {
        if (Objects.nonNull(limit)) {
            Expression offset = limit.getOffset();
            Expression rowCount = limit.getRowCount();
            Integer offsetValue = 0;
            Integer rowCountValue = 0;
            if(offset instanceof JdbcNamedParameter){
                String offsetValueString = JdbcNamedParameter.class.cast(offset).getName();
                offsetValue = Integer.valueOf(offsetValue);
            } else if(offset instanceof LongValue){
                offsetValue = LongValue.class.cast(offset).getBigIntegerValue().intValue();
            } else {
                throw new SqlParserException("无法解析Limit中offset参数");
            }

            if (rowCount instanceof JdbcNamedParameter) {
                String rowCountValueString = JdbcNamedParameter.class.cast(rowCount).getName();
                rowCountValue = Integer.valueOf(rowCountValueString);
            } else if(rowCount instanceof LongValue){
                rowCountValue = LongValue.class.cast(rowCount).getBigIntegerValue().intValue();
            } else {
                throw new SqlParserException("无法解析Limit中rowCount参数");
            }
            return new LimitData(offsetValue, rowCountValue);
        }
        return null;
    }
}
