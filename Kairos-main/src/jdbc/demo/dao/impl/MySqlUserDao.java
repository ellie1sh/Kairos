package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.UserDao;
import jdbc.demo.model.UserRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mindrot.jbcrypt.BCrypt;

public class MySqlUserDao implements UserDao {

    @Override
    public List<UserRecord> findAllUsers() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `user` ORDER BY userId;")) {
            return mapUsers(rs);
        }
    }

    @Override
    public UserRecord findUserById(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM `user` WHERE userId = ? LIMIT 1;")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserRecord> list = mapUsers(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    @Override
    public void addUser(int userId, String firstName, String lastName,
                        String email, String contactNum, String type, String password) throws SQLException {
        String pw = password != null && !password.isBlank()
                ? BCrypt.hashpw(password, BCrypt.gensalt(12))
                : "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try {
                executeAddUserSevenArgs(conn, userId, firstName, lastName, email, contactNum, type, pw);
            } catch (SQLException e) {
                if (!isLegacyAddUserSixParameterError(e)) {
                    throw e;
                }
                executeAddUserSixArgs(conn, userId, firstName, lastName, email, contactNum, type);
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE `user` SET `password` = ? WHERE `userId` = ?")) {
                    upd.setString(1, pw);
                    upd.setInt(2, userId);
                    int n = upd.executeUpdate();
                    if (n != 1) {
                        throw new SQLException(
                                "User row was not updated with password (userId " + userId + "). Check database state.");
                    }
                }
            }
        }
    }

    private static void executeAddUserSevenArgs(Connection conn, int userId, String firstName, String lastName,
                                                String email, String contactNum, String type, String password)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("CALL AddUser(?,?,?,?,?,?,?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            bindOptionalContact(stmt, 5, contactNum);
            stmt.setString(6, type);
            stmt.setString(7, password);
            stmt.execute();
        }
    }

    private static void executeAddUserSixArgs(Connection conn, int userId, String firstName, String lastName,
                                              String email, String contactNum, String type) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("CALL AddUser(?,?,?,?,?,?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            bindOptionalContact(stmt, 5, contactNum);
            stmt.setString(6, type);
            stmt.execute();
        }
    }

    /**
     * True when the server still has the original six-parameter {@code AddUser} procedure
     * (MySQL: "Incorrect number of arguments … expected 6, got 7").
     */
    private static boolean isLegacyAddUserSixParameterError(SQLException e) {
        String m = concatSqlExceptionMessages(e);
        if (m.isEmpty()) {
            return false;
        }
        String u = m.toUpperCase(Locale.ROOT);
        return u.contains("ADDUSER") && u.contains("EXPECTED 6") && u.contains("GOT 7");
    }

    private static String concatSqlExceptionMessages(SQLException e) {
        StringBuilder sb = new StringBuilder();
        for (SQLException x = e; x != null; x = x.getNextException()) {
            String msg = x.getMessage();
            if (msg != null) {
                sb.append(msg);
            }
        }
        return sb.toString();
    }

    @Override
    public void updateUser(int userId, String firstName, String lastName,
                           String email, String contactNum, String type) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL UpdateUser(?,?,?,?,?,?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            bindOptionalContact(stmt, 5, contactNum);
            stmt.setString(6, type);
            stmt.execute();
        }
    }

    @Override
    public void deleteUser(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL DeleteUser(?)")) {
            stmt.setInt(1, userId);
            stmt.execute();
        }
    }

    @Override
    public int getUserBorrowCount(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT sf_user_borrow_count(?) AS borrowCount")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("borrowCount");
            }
        }
    }


    @Override
    public UserRecord findByEmailandPassword(String email, String password) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE email = ? LIMIT 1;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserRecord> list = mapUsers(rs);
                if (list.isEmpty()) return null;

                UserRecord user = list.get(0);
                if (!BCrypt.checkpw(password, user.getPassword())) return null; // wrong password

                return user;
            }
        }
    }

    @Override
    public void changePassword(int userId, String newPlainPassword) throws SQLException {
        String hashed = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt(12));
        String sql = "UPDATE `user` SET password = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashed);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /** {@code contactnum} is nullable and unique; empty string would collide — use SQL NULL. */
    private static void bindOptionalContact(PreparedStatement stmt, int index, String contactNum)
            throws SQLException {
        if (contactNum == null || contactNum.isBlank()) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, contactNum);
        }
    }

    private List<UserRecord> mapUsers(ResultSet rs) throws SQLException {
        List<UserRecord> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new UserRecord(
                    rs.getInt("userId"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("contactnum"),
                    rs.getString("type"),
                    rs.getString("password")
            ));
        }
        return list;
    }
}
