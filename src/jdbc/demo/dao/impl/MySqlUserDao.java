package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.UserDao;
import jdbc.demo.model.UserRecord;

import java.sql.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MySqlUserDao implements UserDao {

    @Override
    public List<UserRecord> findAllUsers() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `user` ORDER BY userId;");
             ResultSet rs = stmt.executeQuery()) {
            return mapUsers(rs);
        }
    }

    @Override
    public UserRecord findUserById(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM `user` WHERE userId = ?;")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UserRecord> list = mapUsers(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    @Override
    public void addUser(int userId, String firstName, String lastName,
                        String email, String contactNum, String type) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL AddUser(?,?,?,?,?,?)}")) {
            cs.setInt(1, userId);
            cs.setString(2, firstName);
            cs.setString(3, lastName);
            cs.setString(4, email);
            cs.setString(5, contactNum);
            cs.setString(6, type);
            cs.execute();
        }
    }

    @Override
    public void updateUser(int userId, String firstName, String lastName,
                           String email, String contactNum, String type) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL UpdateUser(?,?,?,?,?,?)}")) {
            cs.setInt(1, userId);
            cs.setString(2, firstName);
            cs.setString(3, lastName);
            cs.setString(4, email);
            cs.setString(5, contactNum);
            cs.setString(6, type);
            cs.execute();
        }
    }

    @Override
    public void deleteUser(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL DeleteUser(?)}")) {
            cs.setInt(1, userId);
            cs.execute();
        }
    }

    @Override
    public int getUserBorrowCount(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{? = CALL sf_user_borrow_count(?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setInt(2, userId);
            cs.execute();
            return cs.getInt(1);
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
                    rs.getString("type")
            ));
        }
        return list;
    }
}
