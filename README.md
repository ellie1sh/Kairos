# Kairos Borrowing System — ITCS 221L Finals Project

**Team Kairos** | 2nd Semester AY 2025–2026

---

## Overview

A console-based Java application that manages equipment borrowing for a school facility. Users log in by selecting a role (Custodian, Borrower, or Admin) and interact with a MySQL database through a menu-driven interface. All create, update, and delete operations go through stored procedures; read-only reports use prepared statements; computed labels use stored functions invoked via `CallableStatement`.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java (JDK) | 17 or later |
| Maven | 3.6 or later |
| MySQL Server | 8.x |

---

## Database Setup

1. Open MySQL (Workbench, phpMyAdmin, or CLI).
2. Run the entire `221Team-Kairos-Act4.sql` file. This will:
   - Create the database schema (`kairosact4`)
   - Insert sample data for all tables
   - Create all stored **procedures** (e.g. `AddItem`, `CreateBorrow`, `ApproveActivity`)
   - Create all stored **functions** (e.g. `sf_item_availability_label`, `sf_borrow_duration_days`)
3. The default connection in `DatabaseConnection.java` uses:
   - Host: `localhost:3306`
   - Database: `kairosact4`
   - User: `root`
   - Password: *(empty)*

   Change these in `src/jdbc/demo/config/DatabaseConnection.java` if your setup differs.

---

## Build & Run

```bash
# from the workspace root (where pom.xml is)
mvn package -q
java -jar target/infomanmidtermprojectteamkairos-1.0-SNAPSHOT.jar
```

The program verifies the database connection before showing any menu. If MySQL is not running or the database does not exist, it will print a specific error message and exit cleanly.

---

## Project Structure

```
workspace/
├── pom.xml                               Maven build file
├── 221Team-Kairos-Act4.sql               Full database schema + data + routines
├── 221Team-Kairos-Act4.pdf               Project requirements and CRUD matrix
└── src/jdbc/demo/
    ├── Main.java                         Entry point — all menus and user interaction
    ├── Database.java                     Facade that delegates to individual DAOs
    ├── config/
    │   └── DatabaseConnection.java       JDBC connection (URL, user, password)
    ├── model/
    │   ├── UserRecord.java
    │   ├── ItemRecord.java
    │   ├── BorrowRecord.java
    │   ├── ActivityRecord.java
    │   └── QueryResult.java              Generic result holder for report queries
    ├── dao/
    │   ├── UserDao.java                  Interface
    │   ├── ItemDao.java
    │   ├── BorrowDao.java
    │   ├── ActivityDao.java
    │   └── ReportDao.java
    └── dao/impl/
        ├── MySqlUserDao.java             Implementation — uses CallableStatement for writes
        ├── MySqlItemDao.java
        ├── MySqlBorrowDao.java
        ├── MySqlActivityDao.java
        └── MySqlReportDao.java           All 30 read-only report queries
```

---

## How the Code Is Layered

```
Main.java  →  Database.java  →  *Dao interface  →  MySql*Dao  →  MySQL
  (UI)          (facade)         (contract)        (JDBC)       (stored routines)
```

- **`Main.java`** handles all console I/O — prompts, menus, and formatting. It never touches JDBC directly.
- **`Database.java`** is a single object that `Main` holds. It exposes one method per feature and delegates to the correct DAO.
- **DAO interfaces** define the contract for each domain (User, Item, Borrow, Activity, Report).
- **`MySql*Dao` classes** are the only code that opens JDBC connections. Write operations call stored procedures; computed labels call stored functions.
- **`MySqlReportDao`** handles all 30 read queries. The connection is set to read-only mode for those queries.

---

## Database Schema

```
user          userId, firstName, lastName, email, contactnum, type
              type ∈ {Student, Professor, Custodian}

item          itemId, itemName, itemType, description, model,
              conditionStatus, availabilityStatus, dateAcquired
              itemType         ∈ {tool, accessory, peripheral, equipment}
              conditionStatus  ∈ {working, damaged, under maintenance}
              availabilityStatus ∈ {available, borrowed, unavailable}

facility      facilityId, facilityName

activity      activityId, requesterId → user, approvedBy → user,
              requestDate, status, activityName, activityType,
              activityDate, remarks
              status ∈ {Pending, Approved, Rejected}

activitydetails  activityId → activity, facilityId → facility

borrow        borrowId, borrowerId → user, custodianId → user,
              dateBorrowed, timeBorrowed, activityId → activity,
              dateReturned, timeReturned, status, remarks
              status ∈ {borrowed, returned, returned with damage, rejected}

borrowdetails    borrowId → borrow, itemId → item
```

All foreign keys use `ON DELETE CASCADE ON UPDATE CASCADE`.

---

## Stored Procedures (in SQL file)

Every write operation is a stored procedure called from Java via `CallableStatement`.

| Procedure | What it does |
|---|---|
| `AddUser` | Insert a new user; validates type |
| `UpdateUser` | Update all user fields |
| `DeleteUser` | Delete user (cascades to activity/borrow) |
| `AddFacility` | Insert a new facility |
| `UpdateFacility` | Rename a facility |
| `DeleteFacility` | Delete facility (cascades to activitydetails) |
| `AddItem` | Insert a new item; validates type, condition, availability |
| `UpdateItem` | Update item name, type, description, model, dateAcquired |
| `UpdateItemStatus` | Update condition and availability only |
| `MarkItemUnderMaintenance` | Sets condition = "under maintenance", availability = "unavailable" |
| `DeleteItem` | Delete item; blocked if currently borrowed |
| `SubmitActivityRequest` | Insert a new Pending activity |
| `AddFacilityToActivity` | Link a facility to an activity |
| `RemoveFacilityFromActivity` | Unlink a facility from an activity |
| `ApproveActivity` | Set status = Approved; only Custodians can approve |
| `RejectActivity` | Set status = Rejected; only from Pending |
| `UpdateActivity` | Edit name/type/date/remarks; only while Pending |
| `DeleteActivity` | Delete activity (cascades to activitydetails/borrow) |
| `CreateBorrow` | Insert a new borrow record; activity must be Approved |
| `AddItemToBorrow` | Add an item to an active borrow; item must be available |
| `RemoveItemFromBorrow` | Remove an item from an active borrow |
| `ReturnBorrow` | Record return date/time; updates item availability automatically |
| `UpdateBorrow` | Change custodian or remarks on a borrow record |
| `DeleteBorrow` | Delete borrow record; blocked if items still borrowed |

---

## Stored Functions (in SQL file)

Stored functions are called from Java using the `{ ? = CALL function_name(?) }` syntax with `CallableStatement` and `registerOutParameter`.

| Function | Parameter | Returns |
|---|---|---|
| `sf_item_availability_label` | `itemId VARCHAR(15)` | `"Available"` / `"Borrowed by <FirstName LastName>"` / `"Unavailable (under maintenance)"` |
| `sf_borrow_duration_days` | `borrowId VARCHAR(6)` | Integer days from `dateBorrowed` to `dateReturned` (or `CURDATE()` if not returned yet). Returns `-1` if record not found. |
| `sf_activity_status_label` | `activityId VARCHAR(6)` | `"Approved by <Name>"` / `"Rejected (<remarks>)"` / `"Pending - awaiting approval"` |
| `sf_user_borrow_count` | `userId INT` | Integer count of all borrow transactions for that user |

**Java usage pattern:**
```java
CallableStatement cs = conn.prepareCall("{? = CALL sf_item_availability_label(?)}");
cs.registerOutParameter(1, Types.VARCHAR);  // position 1 = return value
cs.setString(2, itemId);                    // position 2 = IN parameter
cs.execute();
String label = cs.getString(1);
```

---

## Menu Structure

```
KAIROS BORROWING SYSTEM
  1 - Custodian
  2 - Borrower
  3 - Admin
  0 - Exit

CUSTODIAN MENU
  1 - Manage Equipment (Items)
      1  View all equipment
      2  Find by availability
      3  Find by condition
      4  Find by type
      5  Add new equipment          → CALL AddItem(...)
      6  Update equipment status    → CALL UpdateItemStatus(...)
      7  Mark under maintenance     → CALL MarkItemUnderMaintenance(...)
      8  Delete equipment           → CALL DeleteItem(...)
      9  Check availability label   → { ? = CALL sf_item_availability_label(?) }
  2 - View Users
  3 - Activity Requests
      1  View all activity requests
      2  View sorted (id/name/date/status/type)
      3  Approve an activity        → CALL ApproveActivity(...)
      4  Reject an activity         → CALL RejectActivity(...)
      5  Update an activity         → CALL UpdateActivity(...)
      6  Submit new activity request→ CALL SubmitActivityRequest(...)
      7  Delete an activity         → CALL DeleteActivity(...)
      8  Check activity status label→ { ? = CALL sf_activity_status_label(?) }
  4 - Manage Borrowed Equipment
      1  View all borrow records
      2  View by status
      3  Create borrow record       → CALL CreateBorrow(...)
      4  Add item to borrow         → CALL AddItemToBorrow(...)
      5  Remove item from borrow    → CALL RemoveItemFromBorrow(...)
      6  Record return              → CALL ReturnBorrow(...)
      7  Update borrow record       → CALL UpdateBorrow(...)
      8  Delete borrow record       → CALL DeleteBorrow(...)
      9  Check borrow duration      → { ? = CALL sf_borrow_duration_days(?) }

BORROWER MENU
  1 - Submit Borrow Request         → CALL CreateBorrow(...) + AddItemToBorrow(...)
  2 - View My Borrow History        (filtered to this user's borrowerId only)
  3 - View Available Activities
  4 - My Borrow Count               → { ? = CALL sf_user_borrow_count(?) }

ADMIN MENU
  1 - Manage Users
      1  View all users
      2  Search user by ID          (also shows borrow count via sf_user_borrow_count)
      3  Add user                   → CALL AddUser(...)
      4  Update user                → CALL UpdateUser(...)
      5  Delete user                → CALL DeleteUser(...)
  2 - Track Borrowed Equipment      (view all borrow records)
  3 - Reports                       (30 read-only queries, see below)
```

---

## Reports (Admin → Reports)

All 30 reports use `PreparedStatement` on a read-only connection.

| # | Category | Description |
|---|---|---|
| 1 | Basic | All registered users |
| 2 | Basic | All items in inventory |
| 3 | Basic | All facilities |
| 4 | Basic | Items filtered by availability status |
| 5 | Basic | Find user by ID |
| 6 | Basic | Items filtered by condition status |
| 7 | Basic | Activities on a specific date |
| 8 | Basic | Items filtered by type |
| 9 | Equipment | Currently borrowed items and borrower names |
| 10 | Equipment | Items never borrowed |
| 11 | Equipment | Last person to handle a specific item |
| 12 | Equipment | Borrower history on a specific date |
| 13 | Facility | Activities and their assigned labs |
| 14 | Facility | Facilities used for a specific activity type |
| 15 | Facility | Facility hosting a specific activity |
| 16 | Facility | Facilities with no activities yet |
| 17 | Workflow | Activities and their requesters |
| 18 | Workflow | Activities and their approvers |
| 19 | Workflow | Activities requested by a specific user type |
| 20 | Workflow | Borrow transactions and their custodians |
| 21 | Incidents | Borrowers with incident remarks |
| 22 | Analytics | Count of items currently held per student |
| 23 | Analytics | Most frequently borrowed item |
| 24 | Analytics | Users with no borrow transactions |
| 25 | Analytics | Items used in a specific activity type |
| 26 | Summary | Borrow log filtered by borrower name and/or date range |
| 27 | Summary | Unreturned items and borrower contact |
| 28 | Summary | Items used in a specific facility |
| 29 | Summary | Count of activities approved per custodian |
| 30 | Summary | Item count per item type |

---

## CRUD Matrix (per project spec)

| Actor | Table(s) | C | R | U | D |
|---|---|---|---|---|---|
| Custodian | ITEM | ✅ Add | ✅ View/Filter | ✅ Status | ✅ Delete |
| Custodian | USER | — | ✅ View | — | — |
| Custodian | ACTIVITY / ACTIVITYDETAILS | ✅ Submit | ✅ View/Sort | ✅ Approve/Reject/Edit | ✅ Delete |
| Custodian | BORROW / BORROWDETAILS | ✅ Create | ✅ View/Filter | ✅ Return/Update | ✅ Delete |
| Borrower | BORROW | ✅ Request | ✅ My history | — | — |
| Admin | USER | ✅ Add | ✅ View/Search | ✅ Update | ✅ Delete |
| Admin | BORROW | — | ✅ Track all | — | — |
| Admin | Reports | — | ✅ 30 queries | — | — |

---

## Key Design Decisions

- **Why interfaces?** `UserDao`, `ItemDao`, etc. are interfaces so a different database backend (e.g. PostgreSQL) can be swapped in without touching `Main.java` or `Database.java`.
- **Why a `Database` facade?** `Main.java` works with a single `db` object. It doesn't need to know which DAO handles which table; it just calls `db.addItem(...)` or `db.approveActivity(...)`.
- **Why stored procedures for writes?** Centralises validation logic in the database (e.g. preventing deletion of a borrowed item, enforcing that only Custodians can approve activities). The Java code only needs to handle the `SQLException` signal raised by the procedure.
- **Why stored functions for computed labels?** Functions return a single scalar value and can encapsulate multi-table lookups (e.g. finding who currently holds an item). They keep `Main.java` free of query logic.
- **Why `CallableStatement` for functions?** MySQL does not support calling stored functions directly inside a `PreparedStatement`. The JDBC escape syntax `{ ? = CALL function_name(?) }` with `registerOutParameter` is the correct approach.
