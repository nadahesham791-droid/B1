package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity for i-Wish Server.
 * Supports MySQL / MariaDB by default with configurable parameters.
 */
public class DBConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/iwish_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPass = "root123"; // common default for NetBeans / XAMPP / MySQL

    private static DBConnection instance;

    private DBConnection() {
        try {
            // Load MySQL driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                // Fallback for older driver
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                System.err.println("[DBConnection] Warning: MySQL JDBC driver not found on classpath: " + ex.getMessage());
            }
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public static void configure(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPass = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            System.err.println("[DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getDbUrl() { return dbUrl; }
    public static String getDbUser() { return dbUser; }
}
