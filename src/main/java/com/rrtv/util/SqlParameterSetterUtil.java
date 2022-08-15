package com.rrtv.util;

import com.rrtv.exception.SqlParameterException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class SqlParameterSetterUtil {

    private static final Log logger = LogFactory.getLog(SqlParameterSetterUtil.class);

    private final static String regex = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
            "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|" +
            "table|from|grant|use|group_concat|column_name|" +
            "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
            "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";


    /**
     *  设置参数 支持两种方式设置 一种是 ? 占位符，另一种是 :name 这种形式
     *
     *  约束
     *  ? 占位符 适用于 多个基本类型参数以及集合类型（注意 集合类型的泛型是基本类型和String）
     *    案例： parameterSetter("select id from t where id = ? and name = ?", 100, "wangchangjiu")
     *
     *  :name 占位符 适用于 参数是对象类型
     *     案例：
     *     User user = new User(100, "wangchangjiu")
     *     parameterSetter("select id from t where id = :id and name = :name, user)
     *
     *  String类型的参数 会 做防止 SQL注入 操作
     *
     * @param sql
     * @param parameters
     * @return
     */
    public static String parameterSetter(String sql, @Nullable Object... parameters) {
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                if (sql.contains("?")) {
                    // 使用 ? 占位符
                    if(!isSimpleType(param) && !isCollectionType(param)){
                        throw new SqlParameterException("使用 问号(?) 占位方式设置参数不支持对象类型");
                    }
                    if (isCollectionType(param)) {
                        // 处理集合
                        Iterator iterator = (Collection.class.cast(param)).iterator();
                        StringBuilder sb = new StringBuilder("(");
                        while (iterator.hasNext()) {
                            Object objParam = iterator.next();
                            if (!isSimpleType(objParam)) {
                                // 复杂类型 报错误
                                throw new SqlParameterException("使用 问号(?) 占位方式设置参数，参数是集合类型时，集合元素只支持基本类型");
                            }
                            sb.append(parameterHandle(objParam)).append(",");
                        }
                        sb.deleteCharAt(sb.lastIndexOf(","));
                        sb.append(")");
                        sql = sql.replaceFirst("\\?", sb.toString());
                    } else {
                        sql = sql.replaceFirst("\\?", parameterHandle(param));
                    }
                } else {
                    // 使用 :name 方式占位
                    if(isSimpleType(param)){
                        // 基本类型
                        throw new SqlParameterException("目号(:)占位方式设置参数不支持基本类型");
                    }

                    // 复杂对象
                    AtomicReference<String> reference = new AtomicReference<>(sql);
                    ReflectionUtils.doWithFields(param.getClass(), field -> {
                        field.setAccessible(true);
                        Object paramValue = field.get(param);
                        if(!isSimpleType(paramValue)){
                            // 对象嵌套对象 不支持
                            throw new RuntimeException("");
                        }
                        String setterSql = reference.get().replaceFirst(":" + field.getName(), parameterHandle(paramValue));
                        reference.set(setterSql);
                    });
                    sql = reference.get();
                }
            }
        }

        // 打印日志
        if(logger.isInfoEnabled()){
            logger.info(String.format("设置参数后的SQL: %s", sql));
        }

        return sql;
    }


    /**
     *  参数类型处理
     *  null 处理成 ’null‘,
     *  数字 直接拼接
     *  字符串 先做 防止SQL注入操作 再 拼接 ’‘
     *
     * @param objParam
     * @return
     */
    private static String parameterHandle(Object objParam) {
        StringBuilder sb = new StringBuilder();
        if(objParam == null){
            sb.append("\\'null\\'");
        } else if(objParam instanceof String){
            String param = String.class.cast(objParam);
         //  sb.append("\\'").append(filter(param)).append("\\'");
           sb.append("\\'").append(param).append("\\'");
        } else if(isNumberType(objParam)){
            sb.append(objParam);
        }
        return sb.toString();
    }

    /**
     *  判断是否是数字类型
     * @param var
     * @return
     */
    private static boolean isNumberType(Object var){
        return  Integer.class.isInstance(var) || int.class.isInstance(var)
                || Long.class.isInstance(var) || long.class.isInstance(var)
                || Byte.class.isInstance(var) || byte.class.isInstance(var)
                || Float.class.isInstance(var) || float.class.isInstance(var)
                || Double.class.isInstance(var) || double.class.isInstance(var)
                || BigDecimal.class.isInstance(var);
    }

    /**
     *  判断是否集合类型
     * @param var
     * @return
     */
    private static boolean isCollectionType(Object var){
        return Collection.class.isInstance(var);
    }

    /**
     *  判断是否 简单类型 除去对象类型这类的复杂类型
     * @param var
     * @return
     */
    private static boolean isSimpleType(Object var) {
        return String.class.isInstance(var) || Date.class.isInstance(var)
                || Boolean.class.isInstance(var) || boolean.class.isInstance(var)
                || Character.class.isInstance(var) || char.class.isInstance(var)
                || isNumberType(var);
    }


    /**
     *  SQL 防止注入
     * @param param
     * @return
     */
    public static String filter(String param) {
        return param.replaceAll("(?i)" + regex, ""); // (?i)不区分大小写替换
    }

    public static void main(String[] args) {
        String sql = "select * from t where id = ? and name in ?";
        sql =  parameterSetter(sql );
        System.out.println(sql);

    }

}
