package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.FacilityDao;
import jdbc.demo.model.FacilityRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlFacilityDao implements FacilityDao {

    @Override
    public List<FacilityRecord> findAllFacilities() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT facilityId, facilityName FROM facility ORDER BY facilityId;");
             ResultSet rs = stmt.executeQuery()) {
            return mapFacilities(rs);
        }
    }

    @Override
    public FacilityRecord findFacilityById(String facilityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT facilityId, facilityName FROM facility WHERE facilityId = ?;")) {
            stmt.setString(1, facilityId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<FacilityRecord> list = mapFacilities(rs);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    @Override
    public void addFacility(String facilityId, String facilityName) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL AddFacility(?,?)}")) {
            stmt.setString(1, facilityId);
            stmt.setString(2, facilityName);
            stmt.execute();
        }
    }

    @Override
    public void updateFacility(String facilityId, String facilityName) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL UpdateFacility(?,?)}")) {
            stmt.setString(1, facilityId);
            stmt.setString(2, facilityName);
            stmt.execute();
        }
    }

    @Override
    public void deleteFacility(String facilityId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL DeleteFacility(?)}")) {
            stmt.setString(1, facilityId);
            stmt.execute();
        }
    }

    private List<FacilityRecord> mapFacilities(ResultSet rs) throws SQLException {
        List<FacilityRecord> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new FacilityRecord(
                    rs.getString("facilityId"),
                    rs.getString("facilityName")));
        }
        return list;
    }
}
