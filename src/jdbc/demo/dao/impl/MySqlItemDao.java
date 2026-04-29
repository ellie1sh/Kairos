package jdbc.demo.dao.impl;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.dao.ItemDao;
import jdbc.demo.model.ItemRecord;

import java.sql.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MySqlItemDao implements ItemDao {

    @Override
    public List<ItemRecord> findAllItems() throws SQLException {
        return query("SELECT * FROM item ORDER BY itemId;", null);
    }

    @Override
    public List<ItemRecord> findItemsByAvailability(String availabilityStatus) throws SQLException {
        return query("SELECT * FROM item WHERE availabilityStatus = ? ORDER BY itemId;",
                stmt -> stmt.setString(1, availabilityStatus));
    }

    @Override
    public List<ItemRecord> findItemsByCondition(String conditionStatus) throws SQLException {
        return query("SELECT * FROM item WHERE conditionStatus = ? ORDER BY itemId;",
                stmt -> stmt.setString(1, conditionStatus));
    }

    @Override
    public List<ItemRecord> findItemsByType(String itemType) throws SQLException {
        return query("SELECT * FROM item WHERE itemType = ? ORDER BY itemId;",
                stmt -> stmt.setString(1, itemType));
    }

    @Override
    public ItemRecord findItemById(String itemId) throws SQLException {
        List<ItemRecord> list = query(
                "SELECT * FROM item WHERE itemId = ?;",
                stmt -> stmt.setString(1, itemId));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void addItem(String itemId, String itemName, String itemType,
                        String description, String model,
                        String conditionStatus, String availabilityStatus,
                        String dateAcquired) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL AddItem(?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, itemId);
            cs.setString(2, itemName);
            cs.setString(3, itemType);
            cs.setString(4, description);
            cs.setString(5, model);
            cs.setString(6, conditionStatus);
            cs.setString(7, availabilityStatus);
            cs.setString(8, dateAcquired);
            cs.execute();
        }
    }

    @Override
    public void updateItemStatus(String itemId,
                                 String conditionStatus,
                                 String availabilityStatus) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL UpdateItemStatus(?,?,?)}")) {
            cs.setString(1, itemId);
            cs.setString(2, conditionStatus);
            cs.setString(3, availabilityStatus);
            cs.execute();
        }
    }

    @Override
    public void markItemUnderMaintenance(String itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL MarkItemUnderMaintenance(?)}")) {
            cs.setString(1, itemId);
            cs.execute();
        }
    }

    @Override
    public void deleteItem(String itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL DeleteItem(?)}")) {
            cs.setString(1, itemId);
            cs.execute();
        }
    }

    @Override
    public String getAvailabilityLabel(String itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{? = CALL sf_item_availability_label(?)}")) {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, itemId);
            cs.execute();
            return cs.getString(1);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    private List<ItemRecord> query(String sql, Binder binder) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapItems(rs);
            }
        }
    }

    private List<ItemRecord> mapItems(ResultSet rs) throws SQLException {
        List<ItemRecord> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ItemRecord(
                    rs.getString("itemId"),
                    rs.getString("itemName"),
                    rs.getString("itemType"),
                    rs.getString("description"),
                    rs.getString("model"),
                    rs.getString("conditionStatus"),
                    rs.getString("availabilityStatus"),
                    rs.getString("dateAcquired")
            ));
        }
        return list;
    }
}
