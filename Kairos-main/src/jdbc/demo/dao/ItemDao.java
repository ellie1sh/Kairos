package jdbc.demo.dao;

import jdbc.demo.model.ItemRecord;

import java.sql.SQLException;
import java.util.List;

public interface ItemDao {
    List<ItemRecord> findAllItems() throws SQLException;
    /** sortBy: id, name, type, condition, availability, date (default id). */
    List<ItemRecord> findAllItemsSorted(String sortBy) throws SQLException;
    List<ItemRecord> findItemsByAvailability(String availabilityStatus) throws SQLException;
    List<ItemRecord> findItemsByCondition(String conditionStatus) throws SQLException;
    List<ItemRecord> findItemsByType(String itemType) throws SQLException;
    ItemRecord findItemById(String itemId) throws SQLException;
    void addItem(String itemId, String itemName, String itemType,
                 String description, String model,
                 String conditionStatus, String availabilityStatus,
                 String dateAcquired) throws SQLException;

    /** Updates name, type, description, model, date (procedure {@code UpdateItem}). */
    void updateItem(String itemId, String itemName, String itemType,
                    String description, String model, String dateAcquired) throws SQLException;

    void updateItemStatus(String itemId,
                          String conditionStatus,
                          String availabilityStatus) throws SQLException;
    void markItemUnderMaintenance(String itemId) throws SQLException;
    void deleteItem(String itemId) throws SQLException;

    /** Returns a human-readable availability label for the item (e.g. "Available", "Borrowed by …", "Unavailable (damaged)"). */
    String getAvailabilityLabel(String itemId) throws SQLException;
}
