package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.ActivityDao;
import jdbc.demo.model.ActivityRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlActivityDao implements ActivityDao {

    private static final String SQL_FIND_ALL_ACTIVITIES = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.activityId;
            """;

    private static final String SQL_FIND_APPROVED_ACTIVITIES = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            WHERE a.status = ? GROUP BY a.activityId ORDER BY a.activityDate, a.activityId;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_ID = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.activityId;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_NAME = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.activityName;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_DATE = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.activityDate;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_STATUS = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.status;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_TYPE = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY a.activityType;
            """;

    private static final String SQL_FIND_ACTIVITIES_SORTED_BY_REQUESTER = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            GROUP BY a.activityId ORDER BY req.lastName;
            """;

    private static final String SQL_FIND_ACTIVITIES_BY_REQUESTER_ID = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            WHERE a.requesterId = ? GROUP BY a.activityId ORDER BY a.activityId;
            """;

    private static final String SQL_FIND_ACTIVITY_BY_ID = """
            SELECT a.activityId,
                   a.requesterId AS requesterId,
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
            WHERE a.activityId = ? GROUP BY a.activityId;
            """;

    @Override
    public List<ActivityRecord> findAllActivities() throws SQLException {
        return runQuery(SQL_FIND_ALL_ACTIVITIES, null);
    }

    @Override
    public List<ActivityRecord> findApprovedActivities() throws SQLException {
        return runQuery(SQL_FIND_APPROVED_ACTIVITIES, stmt -> stmt.setString(1, "Approved"));
    }

    @Override
    public List<ActivityRecord> findAllActivitiesSorted(String sortBy) throws SQLException {
        String sql = switch (sortBy.toLowerCase()) {
            case "name"      -> SQL_FIND_ACTIVITIES_SORTED_BY_NAME;
            case "date"      -> SQL_FIND_ACTIVITIES_SORTED_BY_DATE;
            case "status"    -> SQL_FIND_ACTIVITIES_SORTED_BY_STATUS;
            case "type"      -> SQL_FIND_ACTIVITIES_SORTED_BY_TYPE;
            case "requester" -> SQL_FIND_ACTIVITIES_SORTED_BY_REQUESTER;
            default          -> SQL_FIND_ACTIVITIES_SORTED_BY_ID;
        };
        return runQuery(sql, null);
    }

    @Override
    public List<ActivityRecord> findActivitiesByRequesterId(int requesterId) throws SQLException {
        return runQuery(SQL_FIND_ACTIVITIES_BY_REQUESTER_ID, stmt -> stmt.setInt(1, requesterId));
    }

    @Override
    public ActivityRecord findActivityById(String activityId) throws SQLException {
        List<ActivityRecord> list = runQuery(SQL_FIND_ACTIVITY_BY_ID, stmt -> stmt.setString(1, activityId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void submitRequest(String activityId, int requesterId, String requestDate,
                              String activityName, String activityType,
                              String activityDate, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CALL SubmitActivityRequest(?,?,?,?,?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setInt(2, requesterId);
            stmt.setString(3, requestDate);
            stmt.setString(4, activityName);
            stmt.setString(5, activityType);
            stmt.setString(6, activityDate);
            stmt.setString(7, remarks);
            stmt.execute();
        }
    }

    @Override
    public void addFacilityToActivity(String activityId, String facilityId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL AddFacilityToActivity(?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setString(2, facilityId);
            stmt.setInt(3, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void removeFacilityFromActivity(String activityId, String facilityId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CALL RemoveFacilityFromActivity(?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setString(2, facilityId);
            stmt.setInt(3, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void approveActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL ApproveActivity(?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setInt(2, approvedBy);
            stmt.setString(3, remarks);
            stmt.execute();
        }
    }

    @Override
    public void rejectActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL RejectActivity(?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setInt(2, approvedBy);
            stmt.setString(3, remarks);
            stmt.execute();
        }
    }

    @Override
    public void updateActivity(String activityId, String activityName, String activityType,
                               String activityDate, String remarks, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CALL UpdateActivity(?,?,?,?,?,?)")) {
            stmt.setString(1, activityId);
            stmt.setString(2, activityName);
            stmt.setString(3, activityType);
            stmt.setString(4, activityDate);
            stmt.setString(5, remarks);
            stmt.setInt(6, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public void deleteActivity(String activityId, int actorUserId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL DeleteActivity(?,?)")) {
            stmt.setString(1, activityId);
            stmt.setInt(2, actorUserId);
            stmt.execute();
        }
    }

    @Override
    public String getActivityStatusLabel(String activityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT sf_activity_status_label(?) AS statusLabel")) {
            stmt.setString(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getString("statusLabel");
            }
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
                    rs.getInt("requesterId"),
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
