package jdbc.demo.model;

public class ActivityRecord {
    private final String activityId;
    private final String requesterName;
    private final String approvedByName;
    private final String activityName;
    private final String activityType;
    private final String requestDate;
    private final String activityDate;
    private final String status;
    private final String facilityName;
    private final String remarks;

    public ActivityRecord(String activityId, String requesterName, String approvedByName, String activityName,
                          String activityType, String requestDate, String activityDate, String status,
                          String facilityName, String remarks) {
        this.activityId = activityId;
        this.requesterName = requesterName;
        this.approvedByName = approvedByName;
        this.activityName = activityName;
        this.activityType = activityType;
        this.requestDate = requestDate;
        this.activityDate = activityDate;
        this.status = status;
        this.facilityName = facilityName;
        this.remarks = remarks;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public String getStatus() {
        return status;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return String.format(
                "ActivityID: %s | Requester: %s | ApprovedBy: %s | Name: %s | Type: %s | RequestDate: %s | ActivityDate: %s | Status: %s | Facility: %s | Remarks: %s",
                activityId, requesterName, approvedByName, activityName, activityType, requestDate, activityDate, status, facilityName, remarks
        );
    }
}
