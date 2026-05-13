package jdbc.demo.model;

public class FacilityRecord {
    private final String facilityId;
    private final String facilityName;

    public FacilityRecord(String facilityId, String facilityName) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    @Override
    public String toString() {
        return String.format("%s | %s", facilityId, facilityName);
    }
}
