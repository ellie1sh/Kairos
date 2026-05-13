package jdbc.demo.dao;

import jdbc.demo.model.BorrowRecord;

import java.sql.SQLException;
import java.util.List;

public interface BorrowDao {
    List<BorrowRecord> findAllBorrowRecords() throws SQLException;
    List<BorrowRecord> findBorrowRecordsByStatus(String status) throws SQLException;
    BorrowRecord findBorrowById(String borrowId) throws SQLException;

    void createBorrow(String borrowId, int borrowerId, int custodianId,
                      String dateBorrowed, String timeBorrowed,
                      String activityId, String remarks, int actorUserId) throws SQLException;

    void addItemToBorrow(String borrowId, String itemId, int actorUserId) throws SQLException;
    void removeItemFromBorrow(String borrowId, String itemId, int actorUserId) throws SQLException;

    void returnBorrow(String borrowId, String dateReturned, String timeReturned,
                      boolean withDamage, String remarks, int actorUserId) throws SQLException;

    void updateBorrow(String borrowId, int custodianId, String remarks, int actorUserId) throws SQLException;
    void deleteBorrow(String borrowId, int actorUserId) throws SQLException;

    /** Returns borrow records belonging to a specific borrower. */
    List<BorrowRecord> findBorrowsByBorrowerId(int borrowerId) throws SQLException;

    /** Returns the number of days elapsed for a borrow record (uses current date if not yet returned). */
    int getBorrowDurationDays(String borrowId) throws SQLException;
}
