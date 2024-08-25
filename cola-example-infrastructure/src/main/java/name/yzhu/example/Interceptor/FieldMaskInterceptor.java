package name.yzhu.example.Interceptor;
import com.google.common.base.CaseFormat;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
public class FieldMaskInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Statement stmt = (Statement) invocation.getArgs()[0];
        ResultSet rs = stmt.getResultSet();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        HashSet<String> columnNameSet = new HashSet<>();
        HashSet<String> tableNameSet = new HashSet<>();
        for (int i = 1; i < columnCount+1; i++) {
            columnNameSet.add(metaData.getColumnName(i));
            tableNameSet.add(metaData.getTableName(i));
        }
        Object result = invocation.proceed();

        if (result instanceof List) {
            List<?> resultList = (List<?>) result;

            for (Object row : resultList) {
                maskFields(row, columnNameSet);
            }
        }
        return result;
    }

    private void maskFields(Object row, HashSet<String> columnNameSet) throws IllegalAccessException {
        Set<String> maskedFields = new HashSet<>(Arrays.asList("password", "email"));
        for (String name : columnNameSet) {
            if (maskedFields.contains(name)){
                String fieldName = toCamelCase(name);
                try {
                    Field field = row.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(row, "****");
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // Configuration properties can be set here if needed
    }

    public static String toCamelCase(String input) {
        String upperCamelCase = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, input);
        if (upperCamelCase.length() > 0) {
            return Character.toLowerCase(upperCamelCase.charAt(0)) + upperCamelCase.substring(1);
        }
        return upperCamelCase;
    }

}