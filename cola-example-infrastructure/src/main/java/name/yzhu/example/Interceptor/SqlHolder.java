package name.yzhu.example.Interceptor;

public class SqlHolder {
    private static final ThreadLocal<String> sqlHolder = new ThreadLocal<>();

    public static void setSql(String sql) {
        sqlHolder.set(sql);
    }

    public static String getSql() {
        return sqlHolder.get();
    }

    public static void removeSql() {
        sqlHolder.remove();
    }
}
