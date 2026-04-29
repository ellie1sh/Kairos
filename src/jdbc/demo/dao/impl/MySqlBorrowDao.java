package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.BorrowDao;
import jdbc.demo.model.BorrowRecord;

import java.sql.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MySqlBorrowDao implements BorrowDao {

    private static final String SELECT_BORROW_BASE = """
            SELECT b.borrowId,
                   CONCAT(bu.firstName, ' ', bu.lastName) AS borrowerName,
                   CONCAT(cu.firstName, ' ', cu.lastName) AS custodianName,
                   a.activityName,
                   GROUP_CONCAT(i.itemName ORDER BY i.itemId SEPARATOR ', ') AS itemName,
                   b.dateBorrowed,
                   b.timeBorrowed,
                   b.dateReturned,
                   b.timeReturned,
                   b.status,
                   b.remarks
            FROM borrow b
            JOIN `user` bu ON b.borrowerId = bu.userId
            JOIN `user` cu ON b.custodianId = cu.userId
            JOIN activity a ON b.activityId = a.activityId
            LEFT JOIN borrowdetails bd ON b.borrowId = bd.borrowId
            LEFT JOIN item i ON bd.itemId = i.itemId
            """;

    @Override
    public List<BorrowRecord> findAllBorrowRecords() throws SQLException {
        String sql = SELECT_BORROW_BASE + "GROUP BY b.borrowId ORDER BY b.borrowId;";
        return runQuery(sql, null);
    }

    @Override
    public List<BorrowRecord> findBorrowRecordsByStatus(String status) throws SQLException {
        String sql = SELECT_BORROW_BASE + "WHERE b.status = ? GROUP BY b.borrowId ORDER BY b.borrowId;";
        return runQuery(sql, stmt -> stmt.setString(1, status));
    }

    @Override
    public BorrowRecord findBorrowById(String borrowId) throws SQLException {
        String sql = SELECT_BORROW_BASE + "WHERE b.borrowId = ? GROUP BY b.borrowId;";
        List<BorrowRecord> list = runQuery(sql, stmt -> stmt.setString(1, borrowId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void createBorrow(String borrowId, int borrowerId, int custodianId,
                             String dateBorrowed, String timeBorrowed,
                             String activityId, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL CreateBorrow(?,?,?,?,?,?,?)}")) {
            cs.setString(1, borrowId);
            cs.setInt(2, borrowerId);
            cs.setInt(3, custodianId);
            cs.setString(4, dateBorrowed);
            cs.setString(5, timeBorrowed);
            cs.setString(6, activityId);
            cs.setString(7, remarks);
            cs.execute();
        }
    }

    @Override
    public void addItemToBorrow(String borrowId, String itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL AddItemToBorrow(?,?)}")) {
            cs.setString(1, borrowId);
            cs.setString(2, itemId);
            cs.execute();
        }
    }

    @Override
    public void removeItemFromBorrow(String borrowId, String itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL RemoveItemFromBorrow(?,?)}")) {
            cs.setString(1, borrowId);
            cs.setString(2, itemId);
            cs.execute();
        }
    }

    @Override
    public void returnBorrow(String borrowId, String dateReturned, String timeReturned,
                             boolean withDamage, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL ReturnBorrow(?,?,?,?,?)}")) {
            cs.setString(1, borrowId);
            cs.setString(2, dateReturned);
            cs.setString(3, timeReturned);
            cs.setInt(4, withDamage ? 1 : 0);
            cs.setString(5, remarks);
            cs.execute();
        }
    }

    @Override
    public void updateBorrow(String borrowId, int custodianId, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL UpdateBorrow(?,?,?)}")) {
            cs.setString(1, borrowId);
            cs.setInt(2, custodianId);
            cs.setString(3, remarks);
            cs.execute();
        }
    }

    @Override
    public void deleteBorrow(String borrowId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL DeleteBorrow(?)}")) {
            cs.setString(1, borrowId);
            cs.execute();
        }
    }

    @Override
    public List<BorrowRecord> findBorrowsByBorrowerId(int borrowerId) throws SQLException {
        String sql = SELECT_BORROW_BASE +
                "WHERE b.borrowerId = ? GROUP BY b.borrowId ORDER BY b.borrowId;";
        return runQuery(sql, stmt -> stmt.setInt(1, borrowerId));
    }

    @Override
    public int getBorrowDurationDays(String borrowId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{? = CALL sf_borrow_duration_days(?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setString(2, borrowId);
            cs.execute();
            return cs.getInt(1);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private List<BorrowRecord> runQuery(String sql, Binder binder) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapBorrowRecords(rs);
            }
        }
    }

    private List<BorrowRecord> mapBorrowRecords(ResultSet rs) throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        while (rs.next()) {
            records.add(new BorrowRecord(
                    rs.getString("borrowId"),
                    rs.getString("borrowerName"),
                    rs.getString("custodianName"),
                    rs.getString("activityName"),
                    rs.getString("itemName"),
                    rs.getString("dateBorrowed"),
                    rs.getString("timeBorrowed"),
                    rs.getString("dateReturned"),
                    rs.getString("timeReturned"),
                    rs.getString("status"),
                    rs.getString("remarks")
            ));
        }
        return records;
    }
}
