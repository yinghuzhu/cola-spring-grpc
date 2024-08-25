package name.yzhu.example.Interceptor;


import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {MappedStatement.class, ResultHandler.class})
})
public class DataMaskingInterceptor implements Interceptor {

    private List<String> sensitiveFields;

    public DataMaskingInterceptor(List<String> sensitiveFields) {
        this.sensitiveFields = sensitiveFields;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof DefaultResultSetHandler) {
            DefaultResultSetHandler resultSetHandler = (DefaultResultSetHandler) result;
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            ResultHandler resultHandler = (ResultHandler) invocation.getArgs()[1];

            // 获取结果集处理相关的字段
            Field statementContextField;
            try {
                statementContextField = resultSetHandler.getClass().getDeclaredField("statementContext");
                statementContextField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                return result;
            }

            Object statementContext = statementContextField.get(resultSetHandler);
            if (statementContext == null) {
                return result;
            }

            Field resultSetsField;
            try {
                resultSetsField = statementContext.getClass().getDeclaredField("resultSets");
                resultSetsField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                return result;
            }

            Object resultSets = resultSetsField.get(statementContext);
            if (resultSets == null ||!(resultSets instanceof List)) {
                return result;
            }

            List<Object> rows = new ArrayList<>();
            for (Object resultSet : (List<?>) resultSets) {
                Field cursorField;
                try {
                    cursorField = resultSet.getClass().getDeclaredField("cursor");
                    cursorField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    continue;
                }

                Object cursor = cursorField.get(resultSet);
                if (cursor == null) {
                    continue;
                }

                Field rowBoundsField;
                try {
                    rowBoundsField = cursor.getClass().getDeclaredField("rowBounds");
                    rowBoundsField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    continue;
                }

                Object rowBoundsObj = rowBoundsField.get(cursor);
                if (rowBoundsObj == null ||!(rowBoundsObj instanceof RowBounds)) {
                    continue;
                }

                RowBounds rowBounds = (RowBounds) rowBoundsObj;

                Field currentRowField;
                try {
                    currentRowField = cursor.getClass().getDeclaredField("currentRow");
                    currentRowField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    continue;
                }

                int currentRow = (int) currentRowField.get(cursor);

                Field endRowField;
                try {
                    endRowField = rowBounds.getClass().getDeclaredField("endRow");
                    endRowField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    continue;
                }

                int endRow = (int) endRowField.get(rowBounds);

                while (currentRow < endRow) {
                    Object row = null;
                    try {
                        // 获取 ResultSetWrapper
                        Field resultSetWrapperField;
                        try {
                            resultSetWrapperField = cursor.getClass().getDeclaredField("resultSetWrapper");
                            resultSetWrapperField.setAccessible(true);
                        } catch (NoSuchFieldException ex) {
                            continue;
                        }
                        Object resultSetWrapper = resultSetWrapperField.get(cursor);

                        // 获取 ResultMap
                        Field resultMapField;
                        try {
                            resultMapField = statementContext.getClass().getDeclaredField("resultMap");
                            resultMapField.setAccessible(true);
                        } catch (NoSuchFieldException ex) {
                            continue;
                        }
                        Object resultMap = resultMapField.get(statementContext);

                        // 获取 CacheKey 和 ignore 字段（这里假设 ignore 为空）
                        Field cacheKeyField;
                        try {
                            cacheKeyField = cursor.getClass().getDeclaredField("cacheKey");
                            cacheKeyField.setAccessible(true);
                        } catch (NoSuchFieldException ex) {
                            continue;
                        }
                        Object cacheKey = cacheKeyField.get(cursor);
                        String ignore = null;

                        // 调用 getRowValue 方法
//                        row = resultSetHandler.getRowValue(resultSetWrapper, (org.apache.ibatis.mapping.ResultMap) resultMap, cacheKey, ignore, currentRow);
                    } catch (Exception ex) {
                        continue;
                    }
                    rows.add(row);
                    // 模拟增加当前行
                    currentRow++;
                    currentRowField.set(cursor, currentRow);
                }
            }

            for (Object row : rows) {
                for (String field : sensitiveFields) {
                    Object fieldValue = getFieldValue(row, field);
                    if (fieldValue!= null) {
                        setFieldValue(row, field, maskField(fieldValue.toString(), 0));
                    }
                }
            }
        }
        return result;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 可以选择忽略或进行适当的日志记录
        }
    }

    public static String maskField(String value, int maskLength) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        int visibleLength = Math.min(maskLength, length);
        return new String(new char[length - visibleLength]).replace('\0', '*') + value.substring(length - visibleLength);
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}