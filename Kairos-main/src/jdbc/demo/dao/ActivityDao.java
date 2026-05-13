package jdbc.demo.dao;

import jdbc.demo.model.ActivityRecord;

import java.sql.SQLException;
import java.util.List;

public interface ActivityDao {
    List<ActivityRecord> findAllActivities() throws SQLException;
    /** Approved activities only (for borrowers lining up borrows with custodian workflow). */
    List<ActivityRecord> findApprovedActivities() throws SQLException;
    List<ActivityRecord> findAllActivitiesSorted(String sortBy) throws SQLException;
    List<ActivityRecord> findActivitiesByRequesterId(int requesterId) throws SQLException;
    ActivityRecord findActivityById(String activityId) throws SQLException;

    void submitRequest(String activityId, int requesterId, String requestDate,
                       String activityName, String activityType,
                       String activityDate, String remarks) throws SQLException;

    void addFacilityToActivity(String activityId, String facilityId, int actorUserId) throws SQLException;
    void removeFacilityFromActivity(String activityId, String facilityId, int actorUserId) throws SQLException;

    void approveActivity(String activityId, int approvedBy, String remarks) throws SQLException;
    void rejectActivity(String activityId, int approvedBy, String remarks) throws SQLException;

    void updateActivity(String activityId, String activityName, String activityType,
                        String activityDate, String remarks, int actorUserId) throws SQLException;

    void deleteActivity(String activityId, int actorUserId) throws SQLException;

    /** Returns a descriptive status label (e.g. "Approved by …", "Rejected (…)", "Pending - awaiting approval"). */
    String getActivityStatusLabel(String activityId) throws SQLException;
}
