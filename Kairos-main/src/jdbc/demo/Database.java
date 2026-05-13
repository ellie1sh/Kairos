package jdbc.demo;

import jdbc.demo.dao.*;
import jdbc.demo.dao.impl.*;
import jdbc.demo.model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Application facade: routes menu actions from {@link Main} to DAO interfaces ({@link UserDao},
 * {@link ItemDao}, {@link BorrowDao}, {@link ActivityDao}, {@link FacilityDao}, {@link ReportDao}). Interfaces keep JDBC
 * out of the UI and allow swapping implementations (e.g. tests). All JDBC uses
 * {@code PreparedStatement} for parameterized {@code CALL} procedures, reads/updates, and {@code SELECT} for scalar functions.
 */
public class Database {
    private final UserDao userDao;
    private final ItemDao itemDao;
    private final BorrowDao borrowDao;
    private final ActivityDao activityDao;
    private final FacilityDao facilityDao;
    private final ReportDao reportDao;

    public Database() {
        this.userDao      = new MySqlUserDao();
        this.itemDao      = new MySqlItemDao();
        this.borrowDao    = new MySqlBorrowDao();
        this.activityDao  = new MySqlActivityDao();
        this.facilityDao  = new MySqlFacilityDao();
        this.reportDao    = new MySqlReportDao();
    }
    // ---- LOGIN ----

    public UserRecord login(String email, String password) throws SQLException {
        return userDao.findByEmailandPassword(email, password);
    }

    // ---- USER ----

    public List<UserRecord> getAllUsers() throws SQLException {
        return userDao.findAllUsers();
    }

    public UserRecord getUserById(int userId) throws SQLException {
        return userDao.findUserById(userId);
    }

    public void addUser(int userId, String firstName, String lastName,
                        String email, String contactNum, String type, String password) throws SQLException {
        userDao.addUser(userId, firstName, lastName, email, contactNum, type, password);
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

    public List<ItemRecord> getAllItemsSorted(String sortBy) throws SQLException {
        return itemDao.findAllItemsSorted(sortBy);
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

    public void updateItem(String itemId, String itemName, String itemType,
                           String description, String model, String dateAcquired) throws SQLException {
        itemDao.updateItem(itemId, itemName, itemType, description, model, dateAcquired);
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

    public List<ActivityRecord> getApprovedActivities() throws SQLException {
        return activityDao.findApprovedActivities();
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

    public void addFacilityToActivity(String activityId, String facilityId, int actorUserId) throws SQLException {
        activityDao.addFacilityToActivity(activityId, facilityId, actorUserId);
    }

    public void removeFacilityFromActivity(String activityId, String facilityId, int actorUserId) throws SQLException {
        activityDao.removeFacilityFromActivity(activityId, facilityId, actorUserId);
    }

    public void approveActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        activityDao.approveActivity(activityId, approvedBy, remarks);
    }

    public void rejectActivity(String activityId, int approvedBy, String remarks) throws SQLException {
        activityDao.rejectActivity(activityId, approvedBy, remarks);
    }

    public void updateActivity(String activityId, String activityName, String activityType,
                               String activityDate, String remarks, int actorUserId) throws SQLException {
        activityDao.updateActivity(activityId, activityName, activityType, activityDate, remarks, actorUserId);
    }

    public void deleteActivity(String activityId, int actorUserId) throws SQLException {
        activityDao.deleteActivity(activityId, actorUserId);
    }

    public String getActivityStatusLabel(String activityId) throws SQLException {
        return activityDao.getActivityStatusLabel(activityId);
    }

    // ---- FACILITY ----

    public List<FacilityRecord> getAllFacilities() throws SQLException {
        return facilityDao.findAllFacilities();
    }

    public FacilityRecord getFacilityById(String facilityId) throws SQLException {
        return facilityDao.findFacilityById(facilityId);
    }

    public void addFacility(String facilityId, String facilityName) throws SQLException {
        facilityDao.addFacility(facilityId, facilityName);
    }

    public void updateFacility(String facilityId, String facilityName) throws SQLException {
        facilityDao.updateFacility(facilityId, facilityName);
    }

    public void deleteFacility(String facilityId) throws SQLException {
        facilityDao.deleteFacility(facilityId);
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
                             String activityId, String remarks, int actorUserId) throws SQLException {
        borrowDao.createBorrow(borrowId, borrowerId, custodianId,
                dateBorrowed, timeBorrowed, activityId, remarks, actorUserId);
    }

    public void addItemToBorrow(String borrowId, String itemId, int actorUserId) throws SQLException {
        borrowDao.addItemToBorrow(borrowId, itemId, actorUserId);
    }

    public void removeItemFromBorrow(String borrowId, String itemId, int actorUserId) throws SQLException {
        borrowDao.removeItemFromBorrow(borrowId, itemId, actorUserId);
    }

    public void returnBorrow(String borrowId, String dateReturned, String timeReturned,
                             boolean withDamage, String remarks, int actorUserId) throws SQLException {
        borrowDao.returnBorrow(borrowId, dateReturned, timeReturned, withDamage, remarks, actorUserId);
    }

    public void updateBorrow(String borrowId, int custodianId, String remarks, int actorUserId) throws SQLException {
        borrowDao.updateBorrow(borrowId, custodianId, remarks, actorUserId);
    }

    public void deleteBorrow(String borrowId, int actorUserId) throws SQLException {
        borrowDao.deleteBorrow(borrowId, actorUserId);
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
