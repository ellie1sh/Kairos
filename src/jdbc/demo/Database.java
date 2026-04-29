package jdbc.demo;

import jdbc.demo.dao.*;
import jdbc.demo.dao.impl.*;
import jdbc.demo.model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Facade that routes all feature calls to the appropriate DAO.
 */
public class Database {
    private final UserDao userDao;
    private final ItemDao itemDao;
    private final BorrowDao borrowDao;
    private final ActivityDao activityDao;
    private final ReportDao reportDao;

    public Database() {
        this.userDao     = new MySqlUserDao();
        this.itemDao     = new MySqlItemDao();
        this.borrowDao   = new MySqlBorrowDao();
        this.activityDao = new MySqlActivityDao();
        this.reportDao   = new MySqlReportDao();
    }

    // ---- USER ----

    public List<UserRecord> getAllUsers() throws SQLException {
        return userDao.findAllUsers();
    }

    public UserRecord getUserById(int userId) throws SQLException {
        return userDao.findUserById(userId);
    }

    public void addUser(int userId, String firstName, String lastName,
                        String email, String contactNum, String type) throws SQLException {
        userDao.addUser(userId, firstName, lastName, email, contactNum, type);
    }

    public void updateUser(int userId, String firstName, String lastName,
                           String email, String contactNum, String type) throws SQLException {
        userDao.updateUser(userId, firstName, lastName, email, contactNum, type);
    }

    public void deleteUser(int userId) throws SQLException {
        userDao.deleteUser(userId);
    }

    public int getUserBorrowCount(int userId) throws SQLException {
        return userDao.getUserBorrowCount(userId);
    }

    // ---- ITEM ----

    public List<ItemRecord> getAllItems() throws SQLException {
        return itemDao.findAllItems();
    }

    public List<ItemRecord> getItemsByAvailability(String status) throws SQLException {
        return itemDao.findItemsByAvailability(status);
    }

    public List<ItemRecord> getItemsByCondition(String status) throws SQLException {
        return itemDao.findItemsByCondition(status);
    }

    public List<ItemRecord> getItemsByType(String type) throws SQLException {
        return itemDao.findItemsByType(type);
    }

    public ItemRecord getItemById(String itemId) throws SQLException {
        return itemDao.findItemById(itemId);
    }

    public void addItem(String itemId, String itemName, String itemType,
                        String description, String model,
                        String conditionStatus, String availabilityStatus,
                        String dateAcquired) throws SQLException {
        itemDao.addItem(itemId, itemName, itemType, description, model,
                conditionStatus, availabilityStatus, dateAcquired);
    }

    public void updateItemStatus(String itemId, String conditionStatus,
                                 String availabilityStatus) throws SQLException {
        itemDao.updateItemStatus(itemId, conditionStatus, availabilityStatus);
    }

    public void markItemUnderMaintenance(String itemId) throws SQLException {
        itemDao.markItemUnderMaintenance(itemId);
    }

    public void deleteItem(String itemId) throws SQLException {
        itemDao.deleteItem(itemId);
    }

    public String getItemAvailabilityLabel(String itemId) throws SQLException {
        return itemDao.getAvailabilityLabel(itemId);
    }

    // ---- ACTIVITY ----

    public List<ActivityRecord> getAllActivities() throws SQLException {
        return activityDao.findAllActivities();
    }

    public List<ActivityRecord> getAllActivitiesSorted(String sortBy) throws SQLException {
        return activityDao.findAllActivitiesSorted(sortBy);
    }

    public List<ActivityRecord> getActivitiesByRequesterId(int requesterId) throws SQLException {
        return activityDao.findActivitiesByRequesterId(requesterId);
    }

    public ActivityRecord getActivityById(String activityId) throws SQLException {
        return activityDao.findActivityById(activityId);
    }

    public void submitActivityRequest(String activityId, int requesterId, String requestDate,
                                      String activityName, String activityType,
                                      String activityDate, String remarks) throws SQLException {
        activityDao.submitRequest(activityId, requesterId, requestDate,
                activityName, activityType, activityDate, remarks);
    }

    public void addFacilityToActivity(String activityId, String facilityId) throws SQLException {
        activityDao.addFacilityToActivity(activityId, facilityId);
    }

    public void removeFacilityFromActivity(String activityId, String facilityId) throws SQLException {
        activityDao.removeFacilityFromActivity(activityId, facilityId);
    }

    public void approveActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        activityDao.approveActivity(activityId, approvedBy, remarks);
    }

    public void rejectActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        activityDao.rejectActivity(activityId, approvedBy, remarks);
    }

    public void updateActivity(String activityId, String activityName, String activityType,
                               String activityDate, String remarks) throws SQLException {
        activityDao.updateActivity(activityId, activityName, activityType, activityDate, remarks);
    }

    public void deleteActivity(String activityId) throws SQLException {
        activityDao.deleteActivity(activityId);
    }

    public String getActivityStatusLabel(String activityId) throws SQLException {
        return activityDao.getActivityStatusLabel(activityId);
    }

    // ---- BORROW ----

    public List<BorrowRecord> getAllBorrowRecords() throws SQLException {
        return borrowDao.findAllBorrowRecords();
    }

    public List<BorrowRecord> getBorrowRecordsByStatus(String status) throws SQLException {
        return borrowDao.findBorrowRecordsByStatus(status);
    }

    public BorrowRecord getBorrowById(String borrowId) throws SQLException {
        return borrowDao.findBorrowById(borrowId);
    }

    public void createBorrow(String borrowId, int borrowerId, int custodianId,
                             String dateBorrowed, String timeBorrowed,
                             String activityId, String remarks) throws SQLException {
        borrowDao.createBorrow(borrowId, borrowerId, custodianId,
                dateBorrowed, timeBorrowed, activityId, remarks);
    }

    public void addItemToBorrow(String borrowId, String itemId) throws SQLException {
        borrowDao.addItemToBorrow(borrowId, itemId);
    }

    public void removeItemFromBorrow(String borrowId, String itemId) throws SQLException {
        borrowDao.removeItemFromBorrow(borrowId, itemId);
    }

    public void returnBorrow(String borrowId, String dateReturned, String timeReturned,
                             boolean withDamage, String remarks) throws SQLException {
        borrowDao.returnBorrow(borrowId, dateReturned, timeReturned, withDamage, remarks);
    }

    public void updateBorrow(String borrowId, int custodianId, String remarks) throws SQLException {
        borrowDao.updateBorrow(borrowId, custodianId, remarks);
    }

    public void deleteBorrow(String borrowId) throws SQLException {
        borrowDao.deleteBorrow(borrowId);
    }

    public List<BorrowRecord> getBorrowsByBorrowerId(int borrowerId) throws SQLException {
        return borrowDao.findBorrowsByBorrowerId(borrowerId);
    }

    public int getBorrowDurationDays(String borrowId) throws SQLException {
        return borrowDao.getBorrowDurationDays(borrowId);
    }

    // ---- REPORTS ----

    public QueryResult executeReport(int option, List<String> params) throws SQLException {
        return reportDao.executeReport(option, params);
    }
}
