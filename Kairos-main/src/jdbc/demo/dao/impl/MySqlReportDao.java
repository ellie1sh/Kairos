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
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    @Override
    public QueryResult executeReport(int option, List<String> params) throws SQLException {
        return switch (option) {
            case 1 -> runQuery("SELECT * FROM `user`;", null);
            case 2 -> runQuery("SELECT * FROM item;", null);
            case 3 -> runQuery("SELECT * FROM facility;", null);
            case 4 -> runQuery("SELECT * FROM item WHERE availabilityStatus = ?;",
                    bindString(1, getParam(params, 0, "availabilityStatus")));
            case 5 -> runQuery("SELECT * FROM `user` WHERE userId = ?;",
                    bindInt(1, parseIntParam(getParam(params, 0, "userId"), "userId")));
            case 6 -> runQuery("SELECT * FROM item WHERE conditionStatus = ?;",
                    bindString(1, getParam(params, 0, "conditionStatus")));
            case 7 -> runQuery("SELECT * FROM activity WHERE activityDate = ?;",
                    bindString(1, getParam(params, 0, "activityDate")));
            case 8 -> runQuery("SELECT * FROM item WHERE itemType = ?;",
                    bindString(1, getParam(params, 0, "itemType")));
            case 9 -> runQuery("""
                    SELECT i.itemName, u.firstName, u.lastName
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    JOIN borrow b ON bd.borrowId = b.borrowId
                    JOIN `user` u ON b.borrowerId = u.userId
                    WHERE b.status = ?;
                    """, bindString(1, "borrowed"));
            case 10 -> runQuery("""
                    SELECT i.itemName
                    FROM item i
                    LEFT JOIN borrowdetails bd ON i.itemId = bd.itemId
                    WHERE bd.itemId IS NULL;
                    """, null);
            case 11 -> runQuery("""
                    SELECT u.firstName, u.lastName, b.dateReturned
                    FROM `user` u
                    JOIN borrow b ON u.userId = b.borrowerId
                    JOIN borrowdetails bd ON b.borrowId = bd.borrowId
                    WHERE bd.itemId = ?
                    ORDER BY b.dateReturned DESC
                    LIMIT 1;
                    """, bindString(1, getParam(params, 0, "itemId")));
            case 12 -> runQuery("""
                    SELECT i.itemName, u.lastName
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    JOIN borrow b ON bd.borrowId = b.borrowId
                    JOIN `user` u ON b.borrowerId = u.userId
                    WHERE b.dateBorrowed = ?;
                    """, bindString(1, getParam(params, 0, "dateBorrowed")));
            case 13 -> runQuery("""
                    SELECT a.activityName, f.facilityName
                    FROM activity a
                    JOIN activitydetails ad ON a.activityId = ad.activityId
                    JOIN facility f ON ad.facilityId = f.facilityId;
                    """, null);
            case 14 -> runQuery("""
                    SELECT DISTINCT f.facilityName
                    FROM facility f
                    JOIN activitydetails ad ON f.facilityId = ad.facilityId
                    JOIN activity a ON ad.activityId = a.activityId
                    WHERE a.activityType = ?;
                    """, bindString(1, getParam(params, 0, "activityType")));
            case 15 -> runQuery("""
                    SELECT f.facilityName
                    FROM facility f
                    JOIN activitydetails ad ON f.facilityId = ad.facilityId
                    WHERE ad.activityId = ?;
                    """, bindString(1, getParam(params, 0, "activityId")));
            case 16 -> runQuery("""
                    SELECT f.facilityName
                    FROM facility f
                    LEFT JOIN activitydetails ad ON f.facilityId = ad.facilityId
                    WHERE ad.activityId IS NULL;
                    """, null);
            case 17 -> runQuery("""
                    SELECT a.activityName, u.firstName, u.lastName
                    FROM activity a
                    JOIN `user` u ON a.requesterId = u.userId;
                    """, null);
            case 18 -> runQuery("""
                    SELECT a.activityName, COALESCE(u.lastName, 'N/A') AS ApproverName
                    FROM activity a
                    LEFT JOIN `user` u ON a.approvedBy = u.userId;
                    """, null);
            case 19 -> runQuery("""
                    SELECT a.activityName, u.lastName
                    FROM activity a
                    JOIN `user` u ON a.requesterId = u.userId
                    WHERE u.type = ?;
                    """, bindString(1, getParam(params, 0, "userType")));
            case 20 -> runQuery("""
                    SELECT b.borrowId, u.lastName AS CustodianOnDuty
                    FROM borrow b
                    JOIN `user` u ON b.custodianId = u.userId;
                    """, null);
            case 21 -> runQuery("""
                    SELECT DISTINCT u.lastName, i.itemName, b.remarks
                    FROM `user` u
                    JOIN borrow b ON u.userId = b.borrowerId
                    JOIN borrowdetails bd ON b.borrowId = bd.borrowId
                    JOIN item i ON bd.itemId = i.itemId
                    WHERE NULLIF(TRIM(b.remarks), '') IS NOT NULL
                      AND LOWER(b.remarks) REGEXP
                          'incident|broken|damage(d)?|not working|defect|fault|malfunction|missing|lost|cut(s)?|dent(ed)?'
                    ORDER BY u.lastName, i.itemName;
                    """, null);
            case 22 -> runQuery("""
                    SELECT u.lastName, COUNT(bd.itemId) AS totalHeld
                    FROM `user` u
                    JOIN borrow b ON u.userId = b.borrowerId
                    JOIN borrowdetails bd ON b.borrowId = bd.borrowId
                    WHERE b.status = ?
                    GROUP BY u.userId;
                    """, bindString(1, "borrowed"));
            case 23 -> runQuery("""
                    SELECT i.itemName, COUNT(bd.itemId) AS usageCount
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    GROUP BY i.itemId
                    ORDER BY usageCount DESC
                    LIMIT 1;
                    """, null);
            case 24 -> runQuery("""
                    SELECT u.firstName, u.lastName
                    FROM `user` u
                    LEFT JOIN borrow b ON u.userId = b.borrowerId
                    WHERE b.borrowId IS NULL;
                    """, null);
            case 25 -> runQuery("""
                    SELECT i.itemName
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    JOIN borrow b ON bd.borrowId = b.borrowId
                    JOIN activity a ON b.activityId = a.activityId
                    WHERE a.activityType = ?;
                    """, bindString(1, getParam(params, 0, "activityType")));
            case 26 -> runQuery("""
                    SELECT u.lastName, i.itemName, b.dateBorrowed
                    FROM `user` u
                    JOIN borrow b ON u.userId = b.borrowerId
                    JOIN borrowdetails bd ON b.borrowId = bd.borrowId
                    JOIN item i ON bd.itemId = i.itemId
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
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    JOIN borrow b ON bd.borrowId = b.borrowId
                    JOIN `user` u ON b.borrowerId = u.userId
                    WHERE b.dateReturned IS NULL;
                    """, null);
            case 28 -> runQuery("""
                    SELECT DISTINCT i.itemName
                    FROM item i
                    JOIN borrowdetails bd ON i.itemId = bd.itemId
                    JOIN borrow b ON bd.borrowId = b.borrowId
                    JOIN activitydetails ad ON b.activityId = ad.activityId
                    JOIN facility f ON ad.facilityId = f.facilityId
                    WHERE f.facilityName = ?;
                    """, bindString(1, getParam(params, 0, "facilityName")));
            case 29 -> runQuery("""
                    SELECT u.lastName, COUNT(a.activityId)
                    FROM `user` u
                    JOIN activity a ON u.userId = a.approvedBy
                    GROUP BY u.userId;
                    """, null);
            case 30 -> runQuery("SELECT itemType, COUNT(*) FROM item GROUP BY itemType;", null);
            default -> throw new SQLException("Invalid report option.");
        };
    }

    private QueryResult runQuery(String sql, Binder binder) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setReadOnly(true);
            if (binder != null) {
                binder.bind(stmt);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return toQueryResult(rs);
            }
        }
    }

    private Binder bindString(int index, String value) {
        return stmt -> stmt.setString(index, value);
    }

    private Binder bindInt(int index, int value) {
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
