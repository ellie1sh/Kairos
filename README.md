# Kairos Borrowing System — IT221 GUI Project

**Team Kairos** | ITCS 221L Finals · 2nd Semester AY 2025–2026

A Java Swing GUI application for managing equipment borrowing, activity scheduling, and facility usage in a school environment. Backed by a MySQL database using JDBC (Statement, PreparedStatement, and CallableStatement).

---

## Project Structure

```
Kairos-GUI-with-issues/
├── kairosact4_may11.sql                 Full MySQL schema + seed data + stored routines (canonical — use this)
├── README.md                            Backend documentation (stored procedures, schema)
└── Kairos-main/
    ├── pom.xml                          Maven build (Java 17, MySQL connector, FlatLaf)
    └── src/jdbc/demo/
        ├── Main.java                    Entry point — launches the Swing GUI
        ├── Database.java                Facade — routes all GUI calls to DAO interfaces
        ├── config/
        │   └── DatabaseConnection.java  JDBC connection (localhost:3306/kairosact4_may11)
        ├── model/
        │   ├── UserRecord.java
        │   ├── ItemRecord.java
        │   ├── ActivityRecord.java
        │   ├── BorrowRecord.java
        │   ├── FacilityRecord.java
        │   └── QueryResult.java       Generic result holder for the 30 reports
        ├── dao/
        │   ├── UserDao.java             Interface
        │   ├── ItemDao.java
        │   ├── ActivityDao.java
        │   ├── BorrowDao.java
        │   ├── FacilityDao.java
        │   └── ReportDao.java
        ├── dao/impl/
        │   ├── MySqlUserDao.java        PreparedStatement reads + CallableStatement writes
        │   ├── MySqlItemDao.java
        │   ├── MySqlActivityDao.java
        │   ├── MySqlBorrowDao.java
        │   ├── MySqlFacilityDao.java
        │   └── MySqlReportDao.java      All 30 report queries (read-only PreparedStatement)
        └── ui/
            ├── UIColors.java            Colour palette (blue / green / amber / red / navy)
            ├── UIUtils.java             Component factory (buttons, fields, tables, badges)
            ├── MockDataProvider.java    Seed data used in demo mode (no DB required)
            ├── LoginFrame.java          Login window
            ├── AppFrame.java            Main window — sidebar + CardLayout content area
            └── panels/
                ├── DashboardPanel.java  Role-aware stats, charts, recent records
                ├── ItemsPanel.java      Equipment CRUD + filters
                ├── UsersPanel.java      User management
                ├── ActivitiesPanel.java Activity requests, approve / reject
                ├── BorrowsPanel.java    Borrow records, return flow
                ├── FacilitiesPanel.java Facility CRUD
                ├── ReportsPanel.java    All 30 reports with dynamic parameters
                └── ProfilePanel.java    Personal stats + history
```

---

## How to Run

### From an IDE (IntelliJ / Eclipse / NetBeans)

1. Open `Kairos-main/` as a **Maven project**.
2. Run `src/jdbc/demo/Main.java`.
3. The login window opens immediately.

### From the command line

```bash
cd Kairos-main
mvn package -q
java -jar target/infomanmidtermprojectteamkairos-1.0-SNAPSHOT.jar
```

### Demo mode (no MySQL required)

If MySQL is unreachable, the app **automatically switches to Demo Mode**:
- A yellow banner appears at the top of the sidebar.
- All screens are fully navigable with built-in seed data.
- No crashes or error dialogs on startup.

---

## Database Setup

Use **`kairosact4_may11.sql`** at the repo root (same folder as this README). It creates the `kairosact4_may11` database if needed, then loads tables, views, seed data, and stored procedures.

```bash
# From this folder (Kairos-GUI-with-issues), run once:
mysql -u root < kairosact4_may11.sql
```

Default connection settings (`Kairos-main/src/jdbc/demo/config/DatabaseConnection.java`):

| Setting | Value |
|---|---|
| Host | `localhost:3306` |
| Database | `kairosact4_may11` |
| User | `root` |
| Password | *(empty)* |

---

## Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@kairos.edu | admin123 |
| Custodian | jose.reyes@kairos.edu | cust123 |
| Professor | marco.delarosa@kairos.edu | prof123 |
| Student | carlo.bautista@kairos.edu | stud123 |

Login page has quick-fill buttons for each role.

---

## GUI Screens & Role Access

| Screen | Admin | Custodian | Professor | Student |
|---|---|---|---|---|
| Dashboard | ✅ | ✅ | ✅ | ✅ |
| Equipment | ✅ CRUD | ✅ CRUD | — | — |
| Users | ✅ CRUD | ✅ Read | — | — |
| Activities | ✅ Approve/Reject | ✅ Approve/Reject | ✅ Submit | ✅ Submit |
| Borrows | ✅ All records | ✅ All records | ✅ Own only | ✅ Own only |
| Facilities | ✅ CRUD | ✅ CRUD | — | — |
| Reports | ✅ All 30 | — | — | — |
| My Profile | ✅ | ✅ | ✅ | ✅ |

---

## Architecture

```
GUI Panels  →  Database.java (facade)  →  *Dao interface  →  MySql*Dao (JDBC)  →  MySQL
```

- **Reads** use `PreparedStatement` (parameterised queries).
- **Writes** use `CallableStatement` to invoke stored procedures (e.g. `CALL AddItem(...)`, `CALL ApproveActivity(...)`).
- **Computed labels** use stored functions via `{ ? = CALL sf_*() }` escape syntax.
- **Reports** use read-only `PreparedStatement` connections (all 30 queries in `MySqlReportDao`).

---

## Database Schema

| Table | Key columns |
|---|---|
| `user` | userId, firstName, lastName, email, contactnum, type ∈ {Student, Professor, Custodian} |
| `item` | itemId, itemName, itemType, conditionStatus, availabilityStatus, dateAcquired |
| `facility` | facilityId, facilityName |
| `activity` | activityId, requesterId, approvedBy, status ∈ {Pending, Approved, Rejected} |
| `activitydetails` | (activityId, facilityId) — many-to-many |
| `borrow` | borrowId, borrowerId, custodianId, activityId, status ∈ {borrowed, returned, returned with damage, rejected} |
| `borrowdetails` | (borrowId, itemId) |

---

## CRUD Matrix

| Actor | Entity | Create | Read | Update | Delete |
|---|---|---|---|---|---|
| Custodian | Item | ✅ AddItem | ✅ View / Filter | ✅ UpdateItemStatus / MarkMaintenance | ✅ DeleteItem |
| Custodian | User | — | ✅ View all | — | — |
| Custodian | Activity | ✅ SubmitRequest | ✅ View / Sort | ✅ Approve / Reject / Edit | ✅ DeleteActivity |
| Custodian | Borrow | ✅ CreateBorrow + AddItem | ✅ View / Filter | ✅ ReturnBorrow / UpdateBorrow | ✅ DeleteBorrow |
| Borrower | Activity | ✅ SubmitRequest | ✅ Own only | — | — |
| Borrower | Borrow | ✅ CreateBorrow | ✅ Own history | — | — |
| Admin | User | ✅ AddUser | ✅ View / Search | ✅ UpdateUser | ✅ DeleteUser |
| Admin | Borrow | — | ✅ Track all | — | — |
| Admin | Reports | — | ✅ 30 queries | — | — |
