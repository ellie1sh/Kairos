import jdbc.demo.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MigratePasswords {
    public static void main(String[] args) throws SQLException {

        // Step 1: fetch all users with plain-text passwords
        String selectSql = "SELECT userId, password FROM `user`";
        String updateSql = "UPDATE `user` SET password = ? WHERE userId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement selectStmt = conn.createStatement();
             ResultSet rs = selectStmt.executeQuery(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            while (rs.next()) {
                int userId = rs.getInt("userId");
                String plainText = rs.getString("password");

                // Skip already-hashed rows (BCrypt hashes start with $2a$)
                if (plainText != null && plainText.startsWith("$2a$")) {
                    System.out.println("SKIP userId=" + userId + " (already hashed)");
                    continue;
                }

                String hashed = BCrypt.hashpw(plainText, BCrypt.gensalt(12));
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();

                System.out.println("MIGRATED userId=" + userId + " | " + plainText + " → [hashed]");
            }

            System.out.println("\n✅ Migration complete.");
        }
    }
}