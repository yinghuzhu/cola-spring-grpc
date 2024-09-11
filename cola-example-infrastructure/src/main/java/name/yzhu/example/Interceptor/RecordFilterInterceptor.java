package name.yzhu.example.Interceptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.util.Properties;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class RecordFilterInterceptor implements Interceptor{

//    private final ConcurrentMap<String, String> sqlCache = new ConcurrentHashMap<>();
     private final Cache<String, String> sqlCache = CacheBuilder.newBuilder()
        .expireAfterWrite(300, TimeUnit.SECONDS)
        .build();
    private String tableName = "users";
    private String condition = "id > 0";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");

        if (sql != null) {
            try {
                String finalSql = sql;
                sql = sqlCache.get(sql, () -> modifySql(finalSql));
                metaObject.setValue("delegate.boundSql.sql", sql);
            } catch (Exception e) {
                // 记录日志并继续执行原始SQL
                System.err.println("Error modifying SQL: " + e.getMessage());
            }
        }
        return invocation.proceed();
    }

    private String modifySql(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                SelectBody selectBody = selectStatement.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    modifyPlainSelect(selectStatement, (PlainSelect) selectBody);
                } else if (selectBody instanceof SetOperationList) {
                    SetOperationList setOperationList = (SetOperationList) selectBody;
                    for (SelectBody body : setOperationList.getSelects()) {
                        if (body instanceof PlainSelect) {
                            modifyPlainSelect(selectStatement, (PlainSelect) body);
                        }
                    }
                }
                return selectStatement.toString();
            }
        } catch (Exception e) {
            // 记录日志并返回原始SQL
            System.err.println("Error parsing SQL: " + e.getMessage());
        }
        return sql;
    }

    private void modifyPlainSelect(Select selectStatement, PlainSelect plainSelect) throws Exception {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        if (tableList.contains(tableName)) {
            if (plainSelect.getWhere() == null) {
                plainSelect.setWhere(CCJSqlParserUtil.parseCondExpression(condition));
            } else {
                plainSelect.setWhere(CCJSqlParserUtil.parseCondExpression("(" + plainSelect.getWhere() + ") AND " + condition));
            }
        }
    }

    private boolean containsTable(String sql, String tableName) {
        Pattern pattern = Pattern.compile("\\b" + tableName + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        return matcher.find();
    }

    private String addConditionToSql(String originalSql, String condition) {
        if (originalSql.contains("WHERE")) {
            return originalSql + " AND " + condition;
        } else {
            return originalSql + " WHERE " + condition;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里设置一些属性
    }
}
