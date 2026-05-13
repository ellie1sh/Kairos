package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.BorrowDao;
import jdbc.demo.model.BorrowRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlBorrowDao implements BorrowDao {

    private static final String SQL_FIND_ALL_BORROWS = """
            SELECT b.borrowId,
                   b.borrowerId AS borrowerId,
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
            GROUP BY b.borrowId ORDER BY b.borrowId;
            """;

    private static final String SQL_FIND_BORROWS_BY_STATUS = """
            SELECT b.borrowId,
                   b.borrowerId AS borrowerId,
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
            WHERE b.status = ? GROUP BY b.borrowId ORDER BY b.borrowId;
            """;

    private static final String SQL_FIND_BORROW_BY_ID = """
            SELECT b.borrowId,
                   b.borrowerId AS borrowerId,
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
            WHERE b.borrowId = ? GROUP BY b.borrowId;
            """;

    private static final String SQL_FIND_BORROWS_BY_BORROWER = """
            SELECT b.borrowId,
                   b.borrowerId AS borrowerId,
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
            WHERE b.borrowerId = ? GROUP BY b.borrowId ORDER BY b.borrowId;
            """;

    @Override
    public List<BorrowRecord> findAllBorrowRecords() throws SQLException {
        return runQuery(SQL_FIND_ALL_BORROWS, null);
    }

    @Override
    public List<BorrowRecord> findBorrowRecordsByStatus(String status) throws SQLException {
        return runQuery(SQL_FIND_BORROWS_BY_STATUS, stmt -> stmt.setString(1, status));
    }

    @Override
    public BorrowRecord findBorrowById(String borrowId) throws SQLException {
        List<BorrowRecord> list = runQuery(SQL_FIND_BORROW_BY_ID, stmt -> stmt.setString(1, borrowId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void createBorrow(String borrowId, int borrowerId, int custodianId,
                             String dateBorrowed, String timeBorrowed,
                             String activityId, String remarks, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CALL CreateBorrow(?,?,?,?,?,?,?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setInt(2, borrowerId);
            stmt.setInt(3, custodianId);
            stmt.setString(4, dateBorrowed);
            stmt.setString(5, timeBorrowed);
            stmt.setString(6, activityId);
            stmt.setString(7, remarks);
            stmt.setInt(8, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void addItemToBorrow(String borrowId, String itemId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL AddItemToBorrow(?,?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setString(2, itemId);
            stmt.setInt(3, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void removeItemFromBorrow(String borrowId, String itemId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL RemoveItemFromBorrow(?,?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setString(2, itemId);
            stmt.setInt(3, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void returnBorrow(String borrowId, String dateReturned, String timeReturned,
                             boolean withDamage, String remarks, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL ReturnBorrow(?,?,?,?,?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setString(2, dateReturned);
            stmt.setString(3, timeReturned);
            stmt.setInt(4, withDamage ? 1 : 0);
            stmt.setString(5, remarks);
            stmt.setInt(6, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void updateBorrow(String borrowId, int custodianId, String remarks, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL UpdateBorrow(?,?,?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setInt(2, custodianId);
            stmt.setString(3, remarks);
            stmt.setInt(4, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void deleteBorrow(String borrowId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL DeleteBorrow(?,?)")) {
            stmt.setString(1, borrowId);
            stmt.setInt(2, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public List<BorrowRecord> findBorrowsByBorrowerId(int borrowerId) throws SQLException {
        return runQuery(SQL_FIND_BORROWS_BY_BORROWER, stmt -> stmt.setInt(1, borrowerId));
    }

    @Override
    public int getBorrowDurationDays(String borrowId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT sf_borrow_duration_days(?) AS durationDays")) {
            stmt.setString(1, borrowId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("durationDays");
            }
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
                    rs.getInt("borrowerId"),
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
