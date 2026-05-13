package jdbc.demo.dao;

import jdbc.demo.model.FacilityRecord;

import java.sql.SQLException;
import java.util.List;

public interface FacilityDao {
    List<FacilityRecord> findAllFacilities() throws SQLException;

    FacilityRecord findFacilityById(String facilityId) throws SQLException;

    void addFacility(String facilityId, String facilityName) throws SQLException;

    void updateFacility(String facilityId, String facilityName) throws SQLException;

    void deleteFacility(String facilityId) throws SQLException;
}
