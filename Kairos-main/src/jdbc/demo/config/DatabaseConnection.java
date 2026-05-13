package jdbc.demo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC entry point for Kairos. This class only opens connections to an existing database.
 * <p>
 * <b>Schema policy:</b> this application does <em>not</em> create, alter, or drop tables, views,
 * procedures, or any other schema objects. The database must already be provisioned (for example
 * by importing {@code kairosact4_may11.sql} (repo root) in MySQL Workbench or the {@code mysql} CLI).
 * The older {@code 221Team-Kairos-Act4.sql} dump is not used. DAO code uses
 * {@code SELECT} and {@code CALL} only.
 */
public class DatabaseConnection {
    private static final String URL      = "jdbc:mysql://localhost:3306/kairosact4_may11";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC driver not found: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}