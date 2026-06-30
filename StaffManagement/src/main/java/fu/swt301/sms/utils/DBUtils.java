package fu.swt301.sms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    // Biến static để giữ kết nối ảo lúc chạy Integration Test
    private static Connection testConnection = null;

    public static void setTestConnection(Connection conn) {
        testConnection = conn;
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // Nếu đang trong môi trường Test và có kết nối H2, ưu tiên trả về luôn
        if (testConnection != null && !testConnection.isClosed()) {
            return testConnection;
        }

        // Code kết nối SQL Server gốc của bạn 
        Connection conn = null;
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://localhost:1433;databaseName=TestDB";
        conn = DriverManager.getConnection(url, "sa", "sa");
        return conn;
    }
}
