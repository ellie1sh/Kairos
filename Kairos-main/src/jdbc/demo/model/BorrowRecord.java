package jdbc.demo.model;

public class BorrowRecord {
    private final String borrowId;
    /** {@code borrow.borrowerId} — use for "my borrows", not display-name matching. */
    private final int    borrowerId;
    private final String borrowerName;
    private final String custodianName;
    private final String activityName;
    private final String itemName;
    private final String dateBorrowed;
    private final String timeBorrowed;
    private final String dateReturned;
    private final String timeReturned;
    private final String status;
    private final String remarks;

    public BorrowRecord(String borrowId, int borrowerId, String borrowerName, String custodianName,
                        String activityName, String itemName, String dateBorrowed, String timeBorrowed,
                        String dateReturned, String timeReturned, String status, String remarks) {
        this.borrowId = borrowId;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.custodianName = custodianName;
        this.activityName = activityName;
        this.itemName = itemName;
        this.dateBorrowed = dateBorrowed;
        this.timeBorrowed = timeBorrowed;
        this.dateReturned = dateReturned;
        this.timeReturned = timeReturned;
        this.status = status;
        this.remarks = remarks;
    }

    public String getBorrowId() {
        return borrowId;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getCustodianName() {
        return custodianName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDateBorrowed() {
        return dateBorrowed;
    }

    public String getTimeBorrowed() {
        return timeBorrowed;
    }

    public String getDateReturned() {
        return dateReturned;
    }

    public String getTimeReturned() {
        return timeReturned;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return String.format(
                "BorrowID: %s | Borrower: %s | Custodian: %s | Activity: %s | Item: %s | DateBorrowed: %s | TimeBorrowed: %s | DateReturned: %s | TimeReturned: %s | Status: %s | Remarks: %s",
                borrowId, borrowerName, custodianName, activityName, itemName, dateBorrowed, timeBorrowed, dateReturned, timeReturned, status, remarks
        );
    }
}
