package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.ReportDao;
import jdbc.demo.model.QueryResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlReportDao implements ReportDao {
    @FunctionalInterface
    private interface StatementBinder {
        // Used to put values into the ? placeholders.
        void bind(PreparedStatement statement) throws SQLException;
    }

    @Override
    public QueryResult executeReport(int option, List<String> params) throws SQLException {
        return switch (option) {
            case 1 -> runQuery("SELECT * FROM `USER`;", null);
            case 2 -> runQuery("SELECT * FROM ITEM;", null);
            case 3 -> runQuery("SELECT * FROM FACILITY;", null);
            case 4 -> runQuery("SELECT * FROM ITEM WHERE availabilityStatus = ?;",
                    bindString(1, getParam(params, 0, "availabilityStatus")));
            case 5 -> runQuery("SELECT * FROM `USER` WHERE userId = ?;",
                    bindInt(1, parseIntParam(getParam(params, 0, "userId"), "userId")));
            case 6 -> runQuery("SELECT * FROM ITEM WHERE conditionStatus = ?;",
                    bindString(1, getParam(params, 0, "conditionStatus")));
            case 7 -> runQuery("SELECT * FROM ACTIVITY WHERE activityDate = ?;",
                    bindString(1, getParam(params, 0, "activityDate")));
            case 8 -> runQuery("SELECT * FROM ITEM WHERE itemType = ?;",
                    bindString(1, getParam(params, 0, "itemType")));
            case 9 -> runQuery("""
                    SELECT i.itemName, u.firstName, u.lastName
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    JOIN BORROW b ON bd.borrowId = b.borrowId
                    JOIN `USER` u ON b.borrowerId = u.userId
                    WHERE b.status = 'borrowed';
                    """, null);
            case 10 -> runQuery("""
                    SELECT i.itemName
                    FROM ITEM i
                    LEFT JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    WHERE bd.itemId IS NULL;
                    """, null);
            case 11 -> runQuery("""
                    SELECT u.firstName, u.lastName, b.dateReturned
                    FROM `USER` u
                    JOIN BORROW b ON u.userId = b.borrowerId
                    JOIN BORROWDETAILS bd ON b.borrowId = bd.borrowId
                    WHERE bd.itemId = ?
                    ORDER BY b.dateReturned DESC
                    LIMIT 1;
                    """, bindString(1, getParam(params, 0, "itemId")));
            case 12 -> runQuery("""
                    SELECT i.itemName, u.lastName
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    JOIN BORROW b ON bd.borrowId = b.borrowId
                    JOIN `USER` u ON b.borrowerId = u.userId
                    WHERE b.dateBorrowed = ?;
                    """, bindString(1, getParam(params, 0, "dateBorrowed")));
            case 13 -> runQuery("""
                    SELECT a.activityName, f.facilityName
                    FROM ACTIVITY a
                    JOIN ACTIVITYDETAILS ad ON a.activityId = ad.activityId
                    JOIN FACILITY f ON ad.facilityId = f.facilityId;
                    """, null);
            case 14 -> runQuery("""
                    SELECT DISTINCT f.facilityName
                    FROM FACILITY f
                    JOIN ACTIVITYDETAILS ad ON f.facilityId = ad.facilityId
                    JOIN ACTIVITY a ON ad.activityId = a.activityId
                    WHERE a.activityType = ?;
                    """, bindString(1, getParam(params, 0, "activityType")));
            case 15 -> runQuery("""
                    SELECT f.facilityName
                    FROM FACILITY f
                    JOIN ACTIVITYDETAILS ad ON f.facilityId = ad.facilityId
                    WHERE ad.activityId = ?;
                    """, bindString(1, getParam(params, 0, "activityId")));
            case 16 -> runQuery("""
                    SELECT f.facilityName
                    FROM FACILITY f
                    LEFT JOIN ACTIVITYDETAILS ad ON f.facilityId = ad.facilityId
                    WHERE ad.activityId IS NULL;
                    """, null);
            case 17 -> runQuery("""
                    SELECT a.activityName, u.firstName, u.lastName
                    FROM ACTIVITY a
                    JOIN `USER` u ON a.requesterId = u.userId;
                    """, null);
            case 18 -> runQuery("""
                    SELECT a.activityName, u.lastName AS ApproverName
                    FROM ACTIVITY a
                    JOIN `USER` u ON a.approvedBy = u.userId;
                    """, null);
            case 19 -> runQuery("""
                    SELECT a.activityName, u.lastName
                    FROM ACTIVITY a
                    JOIN `USER` u ON a.requesterId = u.userId
                    WHERE u.type = ?;
                    """, bindString(1, getParam(params, 0, "userType")));
            case 20 -> runQuery("""
                    SELECT b.borrowId, u.lastName AS CustodianOnDuty
                    FROM BORROW b
                    JOIN `USER` u ON b.custodianId = u.userId;
                    """, null);
            case 21 -> runQuery("""
                    SELECT DISTINCT u.lastName, i.itemName, b.remarks
                    FROM `USER` u
                    JOIN BORROW b ON u.userId = b.borrowerId
                    JOIN BORROWDETAILS bd ON b.borrowId = bd.borrowId
                    JOIN ITEM i ON bd.itemId = i.itemId
                    WHERE NULLIF(TRIM(b.remarks), '') IS NOT NULL
                      AND LOWER(b.remarks) REGEXP
                          'incident|broken|damage(d)?|not working|defect|fault|malfunction|missing|lost|cut(s)?|dent(ed)?'
                    ORDER BY u.lastName, i.itemName;
                    """, null);
            case 22 -> runQuery("""
                    SELECT u.lastName, COUNT(bd.itemId) AS totalHeld
                    FROM `USER` u
                    JOIN BORROW b ON u.userId = b.borrowerId
                    JOIN BORROWDETAILS bd ON b.borrowId = bd.borrowId
                    WHERE b.status = 'borrowed'
                    GROUP BY u.userId;
                    """, null);
            case 23 -> runQuery("""
                    SELECT i.itemName, COUNT(bd.itemId) AS usageCount
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    GROUP BY i.itemId
                    ORDER BY usageCount DESC
                    LIMIT 1;
                    """, null);
            case 24 -> runQuery("""
                    SELECT u.firstName, u.lastName
                    FROM `USER` u
                    LEFT JOIN BORROW b ON u.userId = b.borrowerId
                    WHERE b.borrowId IS NULL;
                    """, null);
            case 25 -> runQuery("""
                    SELECT i.itemName
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    JOIN BORROW b ON bd.borrowId = b.borrowId
                    JOIN ACTIVITY a ON b.activityId = a.activityId
                    WHERE a.activityType = ?;
                    """, bindString(1, getParam(params, 0, "activityType")));
            case 26 -> runQuery("""
                    SELECT u.lastName, i.itemName, b.dateBorrowed
                    FROM `USER` u
                    JOIN BORROW b ON u.userId = b.borrowerId
                    JOIN BORROWDETAILS bd ON b.borrowId = bd.borrowId
                    JOIN ITEM i ON bd.itemId = i.itemId
                    WHERE (? = '' OR LOWER(u.lastName) LIKE CONCAT('%', LOWER(?), '%'))
                      AND (? = '' OR b.dateBorrowed >= ?)
                      AND (? = '' OR b.dateBorrowed <= ?)
                    ORDER BY b.dateBorrowed DESC, u.lastName, i.itemName;
                    """, stmt -> {
                        String borrowerLastName = getOptionalParam(params, 0);
                        String startDate = getOptionalParam(params, 1);
                        String endDate = getOptionalParam(params, 2);
                        stmt.setString(1, borrowerLastName);
                        stmt.setString(2, borrowerLastName);
                        stmt.setString(3, startDate);
                        stmt.setString(4, startDate);
                        stmt.setString(5, endDate);
                        stmt.setString(6, endDate);
                    });
            case 27 -> runQuery("""
                    SELECT i.itemName, u.lastName, u.contactNum
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    JOIN BORROW b ON bd.borrowId = b.borrowId
                    JOIN `USER` u ON b.borrowerId = u.userId
                    WHERE b.dateReturned IS NULL;
                    """, null);
            case 28 -> runQuery("""
                    SELECT DISTINCT i.itemName
                    FROM ITEM i
                    JOIN BORROWDETAILS bd ON i.itemId = bd.itemId
                    JOIN BORROW b ON bd.borrowId = b.borrowId
                    JOIN ACTIVITYDETAILS ad ON b.activityId = ad.activityId
                    JOIN FACILITY f ON ad.facilityId = f.facilityId
                    WHERE f.facilityName = ?;
                    """, bindString(1, getParam(params, 0, "facilityName")));
            case 29 -> runQuery("""
                    SELECT u.lastName, COUNT(a.activityId)
                    FROM `USER` u
                    JOIN ACTIVITY a ON u.userId = a.approvedBy
                    GROUP BY u.userId;
                    """, null);
            case 30 -> runQuery("SELECT itemType, COUNT(*) FROM ITEM GROUP BY itemType;", null);
            default -> throw new SQLException("Invalid report option.");
        };
    }

    private QueryResult runQuery(String sql, StatementBinder binder) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Mark this connection as read-only.
            conn.setReadOnly(true);
            if (binder != null) {
                binder.bind(stmt);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return toQueryResult(rs);
            }
        }
    }

    private StatementBinder bindString(int index, String value) {
        return stmt -> stmt.setString(index, value);
    }

    private StatementBinder bindInt(int index, int value) {
        return stmt -> stmt.setInt(index, value);
    }

    private String getParam(List<String> params, int index, String name) throws SQLException {
        if (params == null || params.size() <= index) {
            throw new SQLException("Missing required parameter: " + name);
        }
        String value = params.get(index);
        if (value == null || value.trim().isEmpty()) {
            throw new SQLException("Parameter cannot be blank: " + name);
        }
        return value.trim();
    }

    private String getOptionalParam(List<String> params, int index) {
        if (params == null || params.size() <= index || params.get(index) == null) {
            return "";
        }
        return params.get(index).trim();
    }

    private int parseIntParam(String value, String name) throws SQLException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new SQLException("Invalid numeric parameter for " + name + ": " + value);
        }
    }

    private QueryResult toQueryResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> columns = new ArrayList<>();
        // Read column names so we can print any query result in one format.
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                row.add(value == null ? "NULL" : value.toString());
            }
            rows.add(row);
        }
        return new QueryResult(columns, rows);
    }
}
