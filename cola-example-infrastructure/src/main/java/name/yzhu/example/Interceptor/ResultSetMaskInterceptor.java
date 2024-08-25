package name.yzhu.example.Interceptor;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class ResultSetMaskInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Statement stmt = (Statement) invocation.getArgs()[0];
        ResultSet rs = stmt.getResultSet();

        if (rs != null) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String data = rs.getString(i);

                    if (data != null) {
                        String maskedData = "***";
                        rs.updateString(i, maskedData);
                    }
                }
            }
        }

        return invocation.proceed();
    }

    public Object intercept1(Invocation invocation) throws Throwable {
        // 获取原始的 ResultSet
        Statement statement = (Statement) invocation.getArgs()[0];
        ResultSet resultSet = statement.getResultSet();

        if (resultSet != null) {
            // 获取 ResultSet 的元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 定义需要脱敏的字段
            Set<String> maskedFields = new HashSet<>(Arrays.asList("1password"));

            // 创建一个新的 List 来存储处理后的结果
            List<Map<String, Object>> resultList = new ArrayList<>();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    if (maskedFields.contains(columnName)) {
                        value = "****"; // 脱敏处理
                    }
                    row.put(columnName, value);
                }
                resultList.add(row);
            }

            // 返回处理后的结果
            return resultList;
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {

        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        properties.forEach((key, value) -> System.out.println(key + ": " + value));
        // Configuration properties can be set here if needed
    }

}
