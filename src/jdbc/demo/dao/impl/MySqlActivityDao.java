package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.ActivityDao;
import jdbc.demo.model.ActivityRecord;

import java.sql.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MySqlActivityDao implements ActivityDao {

    private static final String SELECT_ACTIVITIES_BASE = """
            SELECT a.activityId,
                   CONCAT(req.firstName, ' ', req.lastName) AS requesterName,
                   COALESCE(CONCAT(app.firstName, ' ', app.lastName), 'N/A') AS approvedByName,
                   a.activityName,
                   a.activityType,
                   a.requestDate,
                   a.activityDate,
                   a.status,
                   a.remarks,
                   GROUP_CONCAT(f.facilityName ORDER BY f.facilityId SEPARATOR ', ') AS facilityName
            FROM activity a
            JOIN `user` req ON a.requesterId = req.userId
            LEFT JOIN `user` app ON a.approvedBy = app.userId
            LEFT JOIN activitydetails ad ON a.activityId = ad.activityId
            LEFT JOIN facility f ON ad.facilityId = f.facilityId
            """;

    @Override
    public List<ActivityRecord> findAllActivities() throws SQLException {
        String sql = SELECT_ACTIVITIES_BASE + "GROUP BY a.activityId ORDER BY a.activityId;";
        return runQuery(sql, null);
    }

    @Override
    public List<ActivityRecord> findAllActivitiesSorted(String sortBy) throws SQLException {
        String column = switch (sortBy.toLowerCase()) {
            case "name"   -> "a.activityName";
            case "date"   -> "a.activityDate";
            case "status" -> "a.status";
            case "type"   -> "a.activityType";
            default       -> "a.activityId";
        };
        String sql = SELECT_ACTIVITIES_BASE + "GROUP BY a.activityId ORDER BY " + column + ";";
        return runQuery(sql, null);
    }

    @Override
    public List<ActivityRecord> findActivitiesByRequesterId(int requesterId) throws SQLException {
        String sql = SELECT_ACTIVITIES_BASE +
                "WHERE a.requesterId = ? GROUP BY a.activityId ORDER BY a.activityId;";
        return runQuery(sql, stmt -> stmt.setInt(1, requesterId));
    }

    @Override
    public ActivityRecord findActivityById(String activityId) throws SQLException {
        String sql = SELECT_ACTIVITIES_BASE +
                "WHERE a.activityId = ? GROUP BY a.activityId;";
        List<ActivityRecord> list = runQuery(sql, stmt -> stmt.setString(1, activityId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void submitRequest(String activityId, int requesterId, String requestDate,
                              String activityName, String activityType,
                              String activityDate, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{CALL SubmitActivityRequest(?,?,?,?,?,?,?)}")) {
            cs.setString(1, activityId);
            cs.setInt(2, requesterId);
            cs.setString(3, requestDate);
            cs.setString(4, activityName);
            cs.setString(5, activityType);
            cs.setString(6, activityDate);
            cs.setString(7, remarks);
            cs.execute();
        }
    }

    @Override
    public void addFacilityToActivity(String activityId, String facilityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL AddFacilityToActivity(?,?)}")) {
            cs.setString(1, activityId);
            cs.setString(2, facilityId);
            cs.execute();
        }
    }

    @Override
    public void removeFacilityFromActivity(String activityId, String facilityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL RemoveFacilityFromActivity(?,?)}")) {
            cs.setString(1, activityId);
            cs.setString(2, facilityId);
            cs.execute();
        }
    }

    @Override
    public void approveActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL ApproveActivity(?,?,?)}")) {
            cs.setString(1, activityId);
            cs.setInt(2, approvedBy);
            cs.setString(3, remarks);
            cs.execute();
        }
    }

    @Override
    public void rejectActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL RejectActivity(?,?,?)}")) {
            cs.setString(1, activityId);
            cs.setInt(2, approvedBy);
            cs.setString(3, remarks);
            cs.execute();
        }
    }

    @Override
    public void updateActivity(String activityId, String activityName, String activityType,
                               String activityDate, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL UpdateActivity(?,?,?,?,?)}")) {
            cs.setString(1, activityId);
            cs.setString(2, activityName);
            cs.setString(3, activityType);
            cs.setString(4, activityDate);
            cs.setString(5, remarks);
            cs.execute();
        }
    }

    @Override
    public void deleteActivity(String activityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL DeleteActivity(?)}")) {
            cs.setString(1, activityId);
            cs.execute();
        }
    }

    @Override
    public String getActivityStatusLabel(String activityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{? = CALL sf_activity_status_label(?)}")) {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, activityId);
            cs.execute();
            return cs.getString(1);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private List<ActivityRecord> runQuery(String sql, Binder binder) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapActivities(rs);
            }
        }
    }

    private List<ActivityRecord> mapActivities(ResultSet rs) throws SQLException {
        List<ActivityRecord> records = new ArrayList<>();
        while (rs.next()) {
            records.add(new ActivityRecord(
                    rs.getString("activityId"),
                    rs.getString("requesterName"),
                    rs.getString("approvedByName"),
                    rs.getString("activityName"),
                    rs.getString("activityType"),
                    rs.getString("requestDate"),
                    rs.getString("activityDate"),
                    rs.getString("status"),
                    rs.getString("facilityName"),
                    rs.getString("remarks")
            ));
        }
        return records;
    }
}
