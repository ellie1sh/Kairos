package jdbc.demo;

import jdbc.demo.config.DatabaseConnection;
import jdbc.demo.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Database db = new Database();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Connecting to database...");
        try (Connection testConn = DatabaseConnection.getConnection()) {
            if (testConn == null || testConn.isClosed()) {
                System.out.println("Unable to connect to the database. Please check your connection settings.");
                return;
            }
            System.out.println("Connected successfully.");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Connection refused")
                    || msg.contains("Communications link failure")
                    || msg.contains("Unable to connect"))) {
                System.out.println("ERROR: Cannot reach the database server.");
                System.out.println("  - Make sure MySQL is running on localhost:3306.");
                System.out.println("  - Verify the database 'kairosact4' exists.");
            } else {
                System.out.println("Failed to connect to the database: " + msg);
            }
            return;
        }

        boolean running = true;
        while (running) {
            printHeader("KAIROS BORROWING SYSTEM");
            System.out.println("  1 - Custodian");
            System.out.println("  2 - Borrower");
            System.out.println("  3 - Admin");
            System.out.println("  0 - Exit");
            printFooter();
            String role = prompt("Select role");
            System.out.println();
            switch (role) {
                case "1" -> custodianMenu();
                case "2" -> borrowerMenu();
                case "3" -> adminMenu();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
            System.out.println();
        }

        scanner.close();
        System.out.println("Program ended.");
    }

    // =========================================================
    // CUSTODIAN MENU
    // =========================================================

    private static void custodianMenu() {
        boolean back = false;
        while (!back) {
            printHeader("CUSTODIAN MENU");
            System.out.println("  1 - Manage Equipment (Items)");
            System.out.println("  2 - View Users");
            System.out.println("  3 - Activity Requests");
            System.out.println("  4 - Manage Borrowed Equipment");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            try {
                switch (opt) {
                    case "1" -> custodianItemMenu();
                    case "2" -> viewUsers();
                    case "3" -> custodianActivityMenu();
                    case "4" -> custodianBorrowMenu();
                    case "0" -> back = true;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void custodianItemMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            printHeader("EQUIPMENT MANAGEMENT");
            System.out.println("  1 - View all equipment");
            System.out.println("  2 - Find equipment by availability");
            System.out.println("  3 - Find equipment by condition");
            System.out.println("  4 - Find equipment by type");
            System.out.println("  5 - Add new equipment");
            System.out.println("  6 - Update equipment status");
            System.out.println("  7 - Mark equipment under maintenance");
            System.out.println("  8 - Delete equipment");
            System.out.println("  9 - Check item availability label");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            switch (opt) {
                case "1" -> {
                    List<ItemRecord> items = db.getAllItems();
                    printItemList(items);
                }
                case "2" -> {
                    String status = prompt("Enter availabilityStatus (available/borrowed/unavailable)");
                    printItemList(db.getItemsByAvailability(status));
                }
                case "3" -> {
                    String cond = prompt("Enter conditionStatus (working/damaged/under maintenance)");
                    printItemList(db.getItemsByCondition(cond));
                }
                case "4" -> {
                    String type = prompt("Enter itemType (tool/accessory/peripheral/equipment)");
                    printItemList(db.getItemsByType(type));
                }
                case "5" -> doAddItem();
                case "6" -> doUpdateItemStatus();
                case "7" -> {
                    String itemId = prompt("Enter itemId to mark under maintenance");
                    ItemRecord current = db.getItemById(itemId);
                    if (current == null) { System.out.println("Item not found."); break; }
                    System.out.println("Current: " + current);
                    String confirm = prompt("Confirm mark under maintenance? (y/n)");
                    if (confirm.equalsIgnoreCase("y")) {
                        db.markItemUnderMaintenance(itemId);
                        System.out.println("Item marked as under maintenance.");
                    }
                }
                case "8" -> {
                    printItemList(db.getAllItems());
                    String itemId = prompt("Enter itemId to delete");
                    ItemRecord current = db.getItemById(itemId);
                    if (current == null) { System.out.println("Item not found."); break; }
                    System.out.println("Will delete: " + current);
                    String confirm = prompt("Confirm delete? (y/n)");
                    if (confirm.equalsIgnoreCase("y")) {
                        db.deleteItem(itemId);
                        System.out.println("Equipment deleted.");
                    }
                }
                case "9" -> {
                    String itemId = prompt("Enter itemId to check availability label");
                    String label = db.getItemAvailabilityLabel(itemId);
                    System.out.println("  Availability label: " + label);
                }
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void doAddItem() throws SQLException {
        System.out.println("--- Add New Equipment ---");
        String itemId    = prompt("Item ID (e.g. I-LT-01)");
        String itemName  = prompt("Item name");
        String itemType  = prompt("Item type (tool/accessory/peripheral/equipment)");
        String desc      = prompt("Description");
        String model     = prompt("Model");
        String cond      = prompt("Condition status (working/damaged/under maintenance)");
        String avail     = prompt("Availability status (available/borrowed/unavailable)");
        String acquired  = prompt("Date acquired (YYYY-MM-DD)");
        db.addItem(itemId, itemName, itemType, desc, model, cond, avail, acquired);
        System.out.println("Equipment added successfully.");
    }

    private static void doUpdateItemStatus() throws SQLException {
        String itemId = prompt("Enter itemId to update");
        ItemRecord current = db.getItemById(itemId);
        if (current == null) { System.out.println("Item not found."); return; }
        System.out.println("Current record:");
        System.out.println("  " + current);
        String cond  = promptWithDefault("New condition status [" + current.getConditionStatus() + "]", current.getConditionStatus());
        String avail = promptWithDefault("New availability status [" + current.getAvailabilityStatus() + "]", current.getAvailabilityStatus());
        db.updateItemStatus(itemId, cond, avail);
        System.out.println("Equipment status updated.");
    }

    private static void custodianActivityMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            printHeader("ACTIVITY REQUESTS");
            System.out.println("  1 - View all activity requests");
            System.out.println("  2 - View activity requests (sorted)");
            System.out.println("  3 - Approve an activity");
            System.out.println("  4 - Reject an activity");
            System.out.println("  5 - Update an activity");
            System.out.println("  6 - Submit new activity request");
            System.out.println("  7 - Delete an activity");
            System.out.println("  8 - Check activity status label");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            switch (opt) {
                case "1" -> printActivityList(db.getAllActivities());
                case "2" -> {
                    System.out.println("Sort by: id / name / date / status / type");
                    String sortBy = prompt("Sort by");
                    printActivityList(db.getAllActivitiesSorted(sortBy));
                }
                case "3" -> {
                    printActivityList(db.getAllActivities());
                    String actId  = prompt("Enter activityId to approve");
                    ActivityRecord rec = db.getActivityById(actId);
                    if (rec == null) { System.out.println("Activity not found."); break; }
                    System.out.println("Current: " + rec);
                    int approvedBy = Integer.parseInt(prompt("Your userId (Custodian)"));
                    String remarks = promptWithDefault("Remarks (optional)", "");
                    db.approveActivity(actId, approvedBy, remarks);
                    System.out.println("Activity approved.");
                }
                case "4" -> {
                    printActivityList(db.getAllActivities());
                    String actId  = prompt("Enter activityId to reject");
                    ActivityRecord rec = db.getActivityById(actId);
                    if (rec == null) { System.out.println("Activity not found."); break; }
                    System.out.println("Current: " + rec);
                    int approvedBy = Integer.parseInt(prompt("Your userId (Custodian)"));
                    String remarks = promptWithDefault("Remarks / reason (optional)", "");
                    db.rejectActivity(actId, approvedBy, remarks);
                    System.out.println("Activity rejected.");
                }
                case "5" -> doUpdateActivity();
                case "6" -> doSubmitActivityRequest();
                case "7" -> {
                    printActivityList(db.getAllActivities());
                    String actId = prompt("Enter activityId to delete");
                    ActivityRecord rec = db.getActivityById(actId);
                    if (rec == null) { System.out.println("Activity not found."); break; }
                    System.out.println("Will delete: " + rec);
                    String confirm = prompt("Confirm delete? (y/n)");
                    if (confirm.equalsIgnoreCase("y")) {
                        db.deleteActivity(actId);
                        System.out.println("Activity deleted.");
                    }
                }
                case "8" -> {
                    String actId = prompt("Enter activityId to check status label");
                    String label = db.getActivityStatusLabel(actId);
                    System.out.println("  Activity status label: " + label);
                }
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void doSubmitActivityRequest() throws SQLException {
        System.out.println("--- Submit New Activity Request ---");
        String actId     = prompt("New Activity ID (e.g. A013)");
        int requesterId  = Integer.parseInt(prompt("Your userId (requester)"));
        String reqDate   = promptWithDefault("Request date (YYYY-MM-DD)", java.time.LocalDate.now().toString());
        String actName   = prompt("Activity name");
        String actType   = prompt("Activity type (e.g. Seminar/Class Activity/Meeting)");
        String actDate   = prompt("Activity date (YYYY-MM-DD)");
        String remarks   = promptWithDefault("Remarks (optional)", "");
        db.submitActivityRequest(actId, requesterId, reqDate, actName, actType, actDate, remarks);
        System.out.println("Activity request submitted.");

        String addFac = prompt("Link a facility now? (y/n)");
        while (addFac.equalsIgnoreCase("y")) {
            String facId = prompt("Enter facilityId (e.g. F001) or leave blank to finish");
            if (facId.isBlank()) break;
            db.addFacilityToActivity(actId, facId);
            System.out.println("Facility linked.");
            addFac = prompt("Link another facility? (y/n)");
        }
    }

    private static void doUpdateActivity() throws SQLException {
        printActivityList(db.getAllActivities());
        String actId = prompt("Enter activityId to update");
        ActivityRecord rec = db.getActivityById(actId);
        if (rec == null) { System.out.println("Activity not found."); return; }
        System.out.println("Current record:");
        System.out.println("  " + rec);
        String name    = promptWithDefault("Activity name [" + rec.getActivityName() + "]", rec.getActivityName());
        String type    = promptWithDefault("Activity type [" + rec.getActivityType() + "]", rec.getActivityType());
        String date    = promptWithDefault("Activity date [" + rec.getActivityDate() + "]", rec.getActivityDate());
        String remarks = promptWithDefault("Remarks [" + rec.getRemarks() + "]", rec.getRemarks());
        db.updateActivity(actId, name, type, date, remarks);
        System.out.println("Activity updated.");
    }

    private static void custodianBorrowMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            printHeader("MANAGE BORROWED EQUIPMENT");
            System.out.println("  1 - View all borrow records");
            System.out.println("  2 - View borrow records by status");
            System.out.println("  3 - Create borrow record");
            System.out.println("  4 - Add item to borrow");
            System.out.println("  5 - Remove item from borrow");
            System.out.println("  6 - Record return");
            System.out.println("  7 - Update borrow record");
            System.out.println("  8 - Delete borrow record");
            System.out.println("  9 - Check borrow duration (days)");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            switch (opt) {
                case "1" -> printBorrowList(db.getAllBorrowRecords());
                case "2" -> {
                    String status = prompt("Enter status (borrowed/returned/returned with damage/rejected)");
                    printBorrowList(db.getBorrowRecordsByStatus(status));
                }
                case "3" -> doCreateBorrow();
                case "4" -> {
                    printBorrowList(db.getAllBorrowRecords());
                    String bid = prompt("Enter borrowId");
                    BorrowRecord rec = db.getBorrowById(bid);
                    if (rec == null) { System.out.println("Borrow record not found."); break; }
                    System.out.println("Current: " + rec);
                    String itemId = prompt("Enter itemId to add");
                    db.addItemToBorrow(bid, itemId);
                    System.out.println("Item added to borrow.");
                }
                case "5" -> {
                    printBorrowList(db.getAllBorrowRecords());
                    String bid = prompt("Enter borrowId");
                    BorrowRecord rec = db.getBorrowById(bid);
                    if (rec == null) { System.out.println("Borrow record not found."); break; }
                    System.out.println("Current: " + rec);
                    String itemId = prompt("Enter itemId to remove");
                    db.removeItemFromBorrow(bid, itemId);
                    System.out.println("Item removed from borrow.");
                }
                case "6" -> doReturnBorrow();
                case "7" -> {
                    printBorrowList(db.getAllBorrowRecords());
                    String bid = prompt("Enter borrowId to update");
                    BorrowRecord rec = db.getBorrowById(bid);
                    if (rec == null) { System.out.println("Borrow record not found."); break; }
                    System.out.println("Current: " + rec);
                    int custId   = Integer.parseInt(prompt("New custodianId"));
                    String notes = promptWithDefault("Remarks [" + rec.getRemarks() + "]", rec.getRemarks());
                    db.updateBorrow(bid, custId, notes);
                    System.out.println("Borrow record updated.");
                }
                case "8" -> {
                    printBorrowList(db.getAllBorrowRecords());
                    String bid = prompt("Enter borrowId to delete");
                    BorrowRecord rec = db.getBorrowById(bid);
                    if (rec == null) { System.out.println("Borrow record not found."); break; }
                    System.out.println("Will delete: " + rec);
                    String confirm = prompt("Confirm delete? (y/n)");
                    if (confirm.equalsIgnoreCase("y")) {
                        db.deleteBorrow(bid);
                        System.out.println("Borrow record deleted.");
                    }
                }
                case "9" -> {
                    String bid = prompt("Enter borrowId to check duration");
                    int days = db.getBorrowDurationDays(bid);
                    if (days < 0) System.out.println("  Borrow record not found.");
                    else          System.out.println("  Duration: " + days + " day(s).");
                }
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void doCreateBorrow() throws SQLException {
        System.out.println("--- Create Borrow Record ---");
        printActivityList(db.getAllActivities());
        String borrowId    = prompt("Borrow ID (e.g. B013)");
        int    borrowerId  = Integer.parseInt(prompt("Borrower userId"));
        int    custodianId = Integer.parseInt(prompt("Custodian userId"));
        String date        = promptWithDefault("Date borrowed (YYYY-MM-DD)", LocalDate.now().toString());
        String time        = promptWithDefault("Time borrowed (HH:MM:SS)", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        String activityId  = prompt("Activity ID");
        String remarks     = promptWithDefault("Remarks (optional)", "");
        db.createBorrow(borrowId, borrowerId, custodianId, date, time, activityId, remarks);
        System.out.println("Borrow record created.");

        String addItems = prompt("Add items to this borrow now? (y/n)");
        while (addItems.equalsIgnoreCase("y")) {
            printItemList(db.getItemsByAvailability("available"));
            String itemId = prompt("Enter itemId to add (or leave blank to finish)");
            if (itemId.isBlank()) break;
            db.addItemToBorrow(borrowId, itemId);
            System.out.println("Item added.");
            addItems = prompt("Add another item? (y/n)");
        }
    }

    private static void doReturnBorrow() throws SQLException {
        printBorrowList(db.getBorrowRecordsByStatus("borrowed"));
        String bid = prompt("Enter borrowId to return");
        BorrowRecord rec = db.getBorrowById(bid);
        if (rec == null) { System.out.println("Borrow record not found."); return; }
        System.out.println("Current: " + rec);
        String date     = promptWithDefault("Date returned (YYYY-MM-DD)", LocalDate.now().toString());
        String time     = promptWithDefault("Time returned (HH:MM:SS)", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        String damaged  = prompt("Returned with damage? (y/n)");
        String remarks  = promptWithDefault("Remarks (optional)", "");
        db.returnBorrow(bid, date, time, damaged.equalsIgnoreCase("y"), remarks);
        System.out.println("Return recorded.");
    }

    private static void viewUsers() throws SQLException {
        System.out.println("--- All Users ---");
        List<UserRecord> users = db.getAllUsers();
        if (users.isEmpty()) { System.out.println("No users found."); return; }
        for (UserRecord u : users) System.out.println("  " + u);
    }

    // =========================================================
    // BORROWER MENU
    // =========================================================

    private static void borrowerMenu() {
        boolean back = false;
        while (!back) {
            printHeader("BORROWER MENU");
            System.out.println("  1 - Submit Borrow Request");
            System.out.println("  2 - View My Borrow History");
            System.out.println("  3 - View Available Activities");
            System.out.println("  4 - My Borrow Count");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            try {
                switch (opt) {
                    case "1" -> borrowerSubmitRequest();
                    case "2" -> {
                        int uid = Integer.parseInt(prompt("Enter your userId"));
                        UserRecord user = db.getUserById(uid);
                        if (user == null) { System.out.println("User not found."); break; }
                        System.out.println("Borrow history for: " + user);
                        printBorrowList(db.getBorrowsByBorrowerId(uid));
                    }
                    case "3" -> printActivityList(db.getAllActivities());
                    case "4" -> {
                        int uid = Integer.parseInt(prompt("Enter your userId"));
                        UserRecord user = db.getUserById(uid);
                        if (user == null) { System.out.println("User not found."); break; }
                        int count = db.getUserBorrowCount(uid);
                        System.out.println("  " + user.getFirstName() + " " + user.getLastName()
                                + " has " + count + " borrow transaction(s) on record.");
                    }
                    case "0" -> back = true;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered.");
            }
            System.out.println();
        }
    }

    private static void borrowerSubmitRequest() throws SQLException {
        System.out.println("--- Submit Borrow Request ---");
        System.out.println("Available approved activities:");
        printActivityList(db.getAllActivities());
        int uid = Integer.parseInt(prompt("Your userId"));
        UserRecord user = db.getUserById(uid);
        if (user == null) { System.out.println("User not found."); return; }
        System.out.println("Borrower: " + user);

        String borrowId   = prompt("New Borrow ID (e.g. B013)");
        int    custId     = Integer.parseInt(prompt("Custodian userId"));
        String date       = promptWithDefault("Date borrowed (YYYY-MM-DD)", LocalDate.now().toString());
        String time       = promptWithDefault("Time borrowed (HH:MM:SS)", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        String actId      = prompt("Activity ID");
        String remarks    = promptWithDefault("Remarks (optional)", "");

        db.createBorrow(borrowId, uid, custId, date, time, actId, remarks);
        System.out.println("Borrow request submitted.");

        String addItems = prompt("Add items now? (y/n)");
        while (addItems.equalsIgnoreCase("y")) {
            printItemList(db.getItemsByAvailability("available"));
            String itemId = prompt("Enter itemId to add (or leave blank to finish)");
            if (itemId.isBlank()) break;
            db.addItemToBorrow(borrowId, itemId);
            System.out.println("Item added.");
            addItems = prompt("Add another item? (y/n)");
        }
    }

    // =========================================================
    // ADMIN MENU
    // =========================================================

    private static void adminMenu() {
        boolean back = false;
        while (!back) {
            printHeader("ADMIN MENU");
            System.out.println("  1 - Manage Users");
            System.out.println("  2 - Track Borrowed Equipment");
            System.out.println("  3 - Reports");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            try {
                switch (opt) {
                    case "1" -> adminUserMenu();
                    case "2" -> {
                        System.out.println("--- All Borrow Records ---");
                        printBorrowList(db.getAllBorrowRecords());
                    }
                    case "3" -> reportsMenu();
                    case "0" -> back = true;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void adminUserMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            printHeader("USER MANAGEMENT");
            System.out.println("  1 - View all users");
            System.out.println("  2 - Search user by ID");
            System.out.println("  3 - Add user");
            System.out.println("  4 - Update user");
            System.out.println("  5 - Delete user");
            System.out.println("  0 - Back");
            printFooter();
            String opt = prompt("Option");
            System.out.println();
            switch (opt) {
                case "1" -> viewUsers();
                case "2" -> {
                    int uid = Integer.parseInt(prompt("Enter userId"));
                    UserRecord u = db.getUserById(uid);
                    if (u == null) {
                        System.out.println("User not found.");
                    } else {
                        System.out.println("  " + u);
                        int count = db.getUserBorrowCount(uid);
                        System.out.println("  Total borrow transactions (sf_user_borrow_count): " + count);
                    }
                }
                case "3" -> doAddUser();
                case "4" -> doUpdateUser();
                case "5" -> {
                    viewUsers();
                    int uid = Integer.parseInt(prompt("Enter userId to delete"));
                    UserRecord u = db.getUserById(uid);
                    if (u == null) { System.out.println("User not found."); break; }
                    System.out.println("Will delete: " + u);
                    String confirm = prompt("Confirm delete? (y/n)");
                    if (confirm.equalsIgnoreCase("y")) {
                        db.deleteUser(uid);
                        System.out.println("User deleted.");
                    }
                }
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
            System.out.println();
        }
    }

    private static void doAddUser() throws SQLException {
        System.out.println("--- Add New User ---");
        int    uid        = Integer.parseInt(prompt("User ID (number)"));
        String firstName  = prompt("First name");
        String lastName   = prompt("Last name");
        String email      = prompt("Email");
        String contact    = prompt("Contact number");
        String type       = prompt("Type (Student/Professor/Custodian)");
        db.addUser(uid, firstName, lastName, email, contact, type);
        System.out.println("User added successfully.");
    }

    private static void doUpdateUser() throws SQLException {
        viewUsers();
        int uid = Integer.parseInt(prompt("Enter userId to update"));
        UserRecord current = db.getUserById(uid);
        if (current == null) { System.out.println("User not found."); return; }
        System.out.println("Current record:");
        System.out.println("  " + current);
        String firstName = promptWithDefault("First name [" + current.getFirstName() + "]", current.getFirstName());
        String lastName  = promptWithDefault("Last name [" + current.getLastName() + "]", current.getLastName());
        String email     = promptWithDefault("Email [" + current.getEmail() + "]", current.getEmail());
        String contact   = promptWithDefault("Contact [" + current.getContactNum() + "]", current.getContactNum());
        String type      = promptWithDefault("Type [" + current.getType() + "]", current.getType());
        db.updateUser(uid, firstName, lastName, email, contact, type);
        System.out.println("User updated.");
    }

    // =========================================================
    // REPORTS MENU (all 30 read-only queries)
    // =========================================================

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            printHeader("REPORTS");
            System.out.println("Basic System Lookups");
            System.out.println("   1 - List all registered users");
            System.out.println("   2 - View all items in the inventory");
            System.out.println("   3 - List all available facility rooms");
            System.out.println("   4 - Find equipment by availability status");
            System.out.println("   5 - Search for specific userId");
            System.out.println("   6 - Find items by condition status");
            System.out.println("   7 - Activities scheduled on a date");
            System.out.println("   8 - Filter items by type");
            System.out.println("Equipment Accountability");
            System.out.println("   9 - Borrowed items and borrower names");
            System.out.println("  10 - Items never borrowed");
            System.out.println("  11 - Last person to handle a specific item");
            System.out.println("  12 - Borrower history on a specific date");
            System.out.println("Facility & Scheduling");
            System.out.println("  13 - Activities and assigned labs");
            System.out.println("  14 - Facilities used for a specific activity type");
            System.out.println("  15 - Facility hosting a specific activity");
            System.out.println("  16 - Facilities with no activities yet");
            System.out.println("Request Approval & Workflow");
            System.out.println("  17 - Activities and requesters");
            System.out.println("  18 - Activities and approver");
            System.out.println("  19 - Activities requested by specific user type");
            System.out.println("  20 - Borrow transactions and custodian");
            System.out.println("Incident Reporting & Analytics");
            System.out.println("  21 - Borrowers with incident remarks");
            System.out.println("  22 - Count items held per student");
            System.out.println("  23 - Most frequently borrowed item");
            System.out.println("  24 - Users with no borrow transactions");
            System.out.println("  25 - Items out for a specific activity type");
            System.out.println("Transactional Summary");
            System.out.println("  26 - Borrow log (filter by date range/borrower)");
            System.out.println("  27 - Unreturned items and borrower contact");
            System.out.println("  28 - Items used in a specific facility");
            System.out.println("  29 - Count activities approved per custodian");
            System.out.println("  30 - Item count per itemType");
            System.out.println("   0 - Back");
            printFooter();
            String opt = prompt("Choose report");
            System.out.println();
            if (opt.equals("0")) { back = true; continue; }
            try {
                int n = Integer.parseInt(opt);
                List<String> params = collectReportParams(n);
                printQueryResult(db.executeReport(n, params));
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static List<String> collectReportParams(int n) {
        List<String> params = new ArrayList<>();
        switch (n) {
            case 4  -> params.add(prompt("Enter availabilityStatus"));
            case 5  -> params.add(prompt("Enter userId"));
            case 6  -> params.add(prompt("Enter conditionStatus"));
            case 7  -> params.add(prompt("Enter activityDate (YYYY-MM-DD)"));
            case 8  -> params.add(prompt("Enter itemType"));
            case 11 -> params.add(prompt("Enter itemId"));
            case 12 -> params.add(prompt("Enter dateBorrowed (YYYY-MM-DD)"));
            case 14 -> params.add(prompt("Enter activityType"));
            case 15 -> params.add(prompt("Enter activityId"));
            case 19 -> params.add(prompt("Enter user type"));
            case 25 -> params.add(prompt("Enter activityType"));
            case 26 -> {
                params.add(prompt("Borrower lastName (optional, press Enter to skip)"));
                params.add(prompt("Start date YYYY-MM-DD (optional)"));
                params.add(prompt("End date YYYY-MM-DD (optional)"));
            }
            case 28 -> params.add(prompt("Enter facilityName"));
            default -> { /* no params */ }
        }
        return params;
    }

    // =========================================================
    // PRINT HELPERS
    // =========================================================

    private static void printItemList(List<ItemRecord> items) {
        if (items.isEmpty()) { System.out.println("No items found."); return; }
        System.out.printf("%-12s %-18s %-12s %-20s %-18s %-12s%n",
                "ItemID", "Name", "Type", "Condition", "Availability", "Acquired");
        System.out.println("-".repeat(98));
        for (ItemRecord i : items) {
            System.out.printf("%-12s %-18s %-12s %-20s %-18s %-12s%n",
                    i.getItemId(), i.getItemName(), i.getItemType(),
                    i.getConditionStatus(), i.getAvailabilityStatus(), i.getDateAcquired());
        }
    }

    private static void printActivityList(List<ActivityRecord> activities) {
        if (activities.isEmpty()) { System.out.println("No activities found."); return; }
        System.out.printf("%-8s %-22s %-12s %-30s %-20s %-22s %-12s%n",
                "ID", "Requester", "Status", "Activity Name", "Type", "Facility", "Date");
        System.out.println("-".repeat(130));
        for (ActivityRecord a : activities) {
            System.out.printf("%-8s %-22s %-12s %-30s %-20s %-22s %-12s%n",
                    a.getActivityId(),
                    a.getRequesterName(),
                    a.getStatus(),
                    truncate(a.getActivityName(), 29),
                    truncate(a.getActivityType(), 19),
                    a.getFacilityName() == null ? "N/A" : truncate(a.getFacilityName(), 21),
                    a.getActivityDate());
        }
    }

    private static void printBorrowList(List<BorrowRecord> records) {
        if (records.isEmpty()) { System.out.println("No borrow records found."); return; }
        System.out.printf("%-8s %-22s %-22s %-20s %-22s %-12s %-28s%n",
                "BorrowID", "Borrower", "Custodian", "Activity", "Items", "Date", "Status");
        System.out.println("-".repeat(140));
        for (BorrowRecord b : records) {
            System.out.printf("%-8s %-22s %-22s %-20s %-22s %-12s %-28s%n",
                    b.getBorrowId(),
                    b.getBorrowerName(),
                    b.getCustodianName(),
                    truncate(b.getActivityName(), 19),
                    b.getItemName() == null ? "N/A" : truncate(b.getItemName(), 21),
                    b.getDateBorrowed() == null ? "N/A" : b.getDateBorrowed(),
                    b.getStatus());
        }
    }

    private static void printQueryResult(QueryResult result) {
        if (result.getRows().isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        List<String> columns = result.getColumns();
        List<List<String>> rows = result.getRows();
        List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            int max = columns.get(i).length();
            for (List<String> row : rows) {
                if (i < row.size() && row.get(i).length() > max) max = row.get(i).length();
            }
            widths.add(max);
        }
        printRow(columns, widths);
        printSeparator(widths);
        for (List<String> row : rows) printRow(row, widths);
    }

    private static void printRow(List<String> values, List<Integer> widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(String.format("%-" + widths.get(i) + "s", i < values.size() ? values.get(i) : ""));
            if (i < values.size() - 1) sb.append(" | ");
        }
        System.out.println(sb);
    }

    private static void printSeparator(List<Integer> widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.size(); i++) {
            sb.append("-".repeat(widths.get(i)));
            if (i < widths.size() - 1) sb.append("-+-");
        }
        System.out.println(sb);
    }

    private static void printHeader(String title) {
        System.out.println("=".repeat(65));
        System.out.printf("  %s%n", title);
        System.out.println("=".repeat(65));
    }

    private static void printFooter() {
        System.out.println("-".repeat(65));
    }

    private static String prompt(String message) {
        System.out.print(message + ": ");
        return scanner.nextLine().trim();
    }

    private static String promptWithDefault(String message, String defaultValue) {
        System.out.print(message + ": ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
