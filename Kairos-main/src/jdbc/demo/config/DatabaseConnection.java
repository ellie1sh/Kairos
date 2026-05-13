package jdbc.demo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC entry point for Kairos. This class only opens connections to an existing database.
 * <p>
 * <b>Schema policy:</b> this application does <em>not</em> create, alter, or drop tables, views,
 * procedures, or any other schema objects.
 */
public class DatabaseConnection {
    // These are your "Configuration/URL" settings
    private static final String URL      = "jdbc:mysql://localhost:3306/kairosact4_may11";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Error Message: Driver file missing
            throw new ExceptionInInitializerError("DRIVER ERROR: MySQL JDBC Driver (JAR file) not found in the project. Check your pom.xml.");
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Checks whether a connection can be established and provides specific
     * feedback based on the type of failure.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            int errorCode = e.getErrorCode();

            // Error Message: Connection could not be established
            if (sqlState != null && sqlState.startsWith("08")) {
                System.err.println("CONNECTIVITY ERROR: Could not establish a connection to MySQL.");
            }

            // Error Message: URL did not match
            else if (errorCode == 1049) {
                System.err.println("URL ERROR: The database name in your URL ('kairosact4_may11') was not found.");
            }

            // Error Message: Wrong credentials
            else if (errorCode == 1045) {
                System.err.println("ACCESS ERROR: Invalid username or password.");
            }

            // Other Error Message
            else {
                System.err.println("CONFIGURATION ERROR: The connection URL is invalid or malformed.");
                System.err.println("TECHNICAL MSG: " + e.getMessage());
            }

            return false;
        }
    }
}