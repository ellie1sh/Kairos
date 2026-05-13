package jdbc.demo.ui;

import jdbc.demo.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In-memory seed data used when MySQL is unreachable (demo / offline mode).
 * Mirrors the {@code kairosact4_may11} database schema (see {@code kairosact4_may11.sql} at repo root).
 */
public final class MockDataProvider {

    /** Mutable copy of seed borrows so demo-mode creates/returns/deletes stay consistent across panels. */
    private static final List<BorrowRecord> DEMO_BORROWS = new ArrayList<>();

    static {
        DEMO_BORROWS.addAll(Arrays.asList(
            new BorrowRecord("B001", 6, "Carlo Bautista", "Jose Reyes","IT Club Orientation",       "Laptop Dell XPS, Portable Speaker","2026-05-08","08:00:00",null,        null,       "borrowed",             "Borrowed for orientation"),
            new BorrowRecord("B002", 7, "Nina Garcia",    "Ana Cruz",  "Thesis Defense Dry Run",    "Projector Epson, Wireless Keyboard","2026-05-12","13:00:00","2026-05-12","17:30:00","returned",             ""),
            new BorrowRecord("B003", 4, "Marco Dela Rosa","Jose Reyes","IT221 Final Lab Exam",       "Laptop HP EliteBook, Headset Sony", "2026-05-10","07:30:00","2026-05-10","12:00:00","returned with damage", "Minor scratch on laptop"),
            new BorrowRecord("B004", 9, "Mae Villanueva", "Jose Reyes","Science Fair Project Demo",  "iPad Pro, Webcam Logitech",         "2026-05-02","10:00:00",null,        null,       "borrowed",             ""),
            new BorrowRecord("B005",10, "Diego Ramos",    "Ana Cruz",  "Python Programming Bootcamp","Laptop Dell XPS",                   "2026-05-06","09:00:00","2026-05-06","18:00:00","returned",             "Good condition"),
            new BorrowRecord("B006", 8, "Rico Torres",    "Jose Reyes","Network Troubleshooting",    "Cable Tester, Screwdriver Set",     null,        null,       null,        null,       "rejected",             "Activity not yet approved")
        ));
    }

    private MockDataProvider() {}

    public static List<UserRecord> users() {
        return new ArrayList<>(Arrays.asList(
            new UserRecord(1,  "Maria",  "Santos",    "admin@kairos.edu",             "09171234567", "Admin",     "admin123"),
            new UserRecord(2,  "Jose",   "Reyes",     "jose.reyes@kairos.edu",         "09182345678", "Custodian", "cust123"),
            new UserRecord(3,  "Ana",    "Cruz",      "ana.cruz@kairos.edu",            "09193456789", "Custodian", "cust456"),
            new UserRecord(4,  "Marco",  "Dela Rosa", "marco.delarosa@kairos.edu",      "09204567890", "Professor", "prof123"),
            new UserRecord(5,  "Liza",   "Mendoza",   "liza.mendoza@kairos.edu",        "09215678901", "Professor", "prof456"),
            new UserRecord(6,  "Carlo",  "Bautista",  "carlo.bautista@kairos.edu",      "09226789012", "Student",   "stud123"),
            new UserRecord(7,  "Nina",   "Garcia",    "nina.garcia@kairos.edu",          "09237890123", "Student",   "stud456"),
            new UserRecord(8,  "Rico",   "Torres",    "rico.torres@kairos.edu",          "09248901234", "Student",   "stud789"),
            new UserRecord(9,  "Mae",    "Villanueva","mae.villanueva@kairos.edu",       "09259012345", "Student",   "stud012"),
            new UserRecord(10, "Diego",  "Ramos",     "diego.ramos@kairos.edu",          "09260123456", "Student",   "stud345")
        ));
    }

    public static List<ItemRecord> items() {
        return new ArrayList<>(Arrays.asList(
            new ItemRecord("I-LT-01","Laptop Dell XPS",      "equipment",  "Dell XPS 15 laptop",           "Dell XPS 15 9500",       "working",          "available",   "2022-06-15"),
            new ItemRecord("I-LT-02","Laptop HP EliteBook",   "equipment",  "HP EliteBook business laptop", "HP EliteBook 840 G8",    "working",          "borrowed",    "2022-06-15"),
            new ItemRecord("I-PJ-01","Projector Epson",       "equipment",  "Full HD classroom projector",  "Epson EB-X51",           "working",          "available",   "2021-08-20"),
            new ItemRecord("I-PJ-02","Projector BenQ",        "equipment",  "Interactive projector",        "BenQ MW550",             "damaged",          "unavailable", "2021-08-20"),
            new ItemRecord("I-MK-01","Wireless Keyboard",     "peripheral", "Logitech wireless keyboard",   "Logitech K780",          "working",          "available",   "2023-01-10"),
            new ItemRecord("I-MS-01","Wireless Mouse",        "peripheral", "Logitech wireless mouse",      "Logitech MX Master 3",   "working",          "available",   "2023-01-10"),
            new ItemRecord("I-HS-01","Headset Sony",          "accessory",  "Sony noise-canceling headset", "Sony WH-1000XM4",        "working",          "borrowed",    "2023-03-05"),
            new ItemRecord("I-CA-01","Webcam Logitech",       "peripheral", "1080p USB webcam",             "Logitech C920",          "working",          "available",   "2023-03-05"),
            new ItemRecord("I-EX-01","Extension Cord",        "accessory",  "10-outlet surge extension",    "Meiji E-1013",           "working",          "available",   "2022-09-01"),
            new ItemRecord("I-TB-01","iPad Pro",              "equipment",  "Apple iPad for presentations", "iPad Pro 12.9 M2",       "under maintenance","unavailable", "2023-07-20"),
            new ItemRecord("I-SC-01","Document Scanner",      "equipment",  "Fujitsu document scanner",     "Fujitsu ScanSnap iX1600","working",          "available",   "2022-11-15"),
            new ItemRecord("I-SD-01","Portable Speaker",      "accessory",  "JBL portable Bluetooth speaker","JBL Charge 5",          "working",          "available",   "2023-02-28"),
            new ItemRecord("I-TL-01","Screwdriver Set",       "tool",       "Electronics precision set",    "iFixit Pro Tech",        "working",          "available",   "2022-04-10"),
            new ItemRecord("I-TL-02","Cable Tester",          "tool",       "Network cable tester",         "Fluke Networks TS30",    "working",          "available",   "2022-04-10")
        ));
    }

    public static List<FacilityRecord> facilities() {
        return new ArrayList<>(Arrays.asList(
            new FacilityRecord("F001","Computer Lab 1"),
            new FacilityRecord("F002","Computer Lab 2"),
            new FacilityRecord("F003","Science Laboratory"),
            new FacilityRecord("F004","AVR Room"),
            new FacilityRecord("F005","Conference Room A"),
            new FacilityRecord("F006","Conference Room B"),
            new FacilityRecord("F007","Library Study Room")
        ));
    }

    public static List<ActivityRecord> activities() {
        return new ArrayList<>(Arrays.asList(
            new ActivityRecord("A001", 4, "Marco Dela Rosa","Jose Reyes",   "IT221 Final Lab Exam",          "Class Activity","2026-04-28","2026-05-10","Approved", "Computer Lab 1",    "Need full lab access"),
            new ActivityRecord("A002", 5, "Liza Mendoza",   null,            "AI Research Seminar",           "Seminar",       "2026-04-30","2026-05-15","Pending",  "AVR Room",          "Guest speaker invited"),
            new ActivityRecord("A003", 6, "Carlo Bautista", "Jose Reyes",   "IT Club Orientation",           "Meeting",       "2026-04-25","2026-05-08","Approved", "Conference Room A", ""),
            new ActivityRecord("A004", 7, "Nina Garcia",    "Ana Cruz",     "Thesis Defense Dry Run",        "Seminar",       "2026-05-01","2026-05-12","Approved", "Conference Room B", "Projector needed"),
            new ActivityRecord("A005", 8, "Rico Torres",    null,            "Network Troubleshooting",       "Class Activity","2026-05-02","2026-05-18","Pending",  null,                "Needs cable testers"),
            new ActivityRecord("A006", 4, "Marco Dela Rosa","Jose Reyes",   "Database Systems Review",       "Class Activity","2026-04-20","2026-05-05","Rejected", "Computer Lab 2",    "Rescheduled"),
            new ActivityRecord("A007", 9, "Mae Villanueva", null,            "Science Fair Project Demo",     "Meeting",       "2026-05-03","2026-05-20","Pending",  "Science Laboratory",""),
            new ActivityRecord("A008", 5, "Liza Mendoza",   "Ana Cruz",     "Python Programming Bootcamp",   "Seminar",       "2026-04-22","2026-05-06","Approved", "Computer Lab 1",    "Two-day event")
        ));
    }

    public static List<BorrowRecord> borrows() {
        return new ArrayList<>(DEMO_BORROWS);
    }

    /** Keeps demo borrows in sync when the user creates a borrow offline. */
    public static void addDemoBorrow(BorrowRecord b) {
        DEMO_BORROWS.add(b);
    }

    public static void replaceDemoBorrow(BorrowRecord updated) {
        for (int i = 0; i < DEMO_BORROWS.size(); i++) {
            if (DEMO_BORROWS.get(i).getBorrowId().equals(updated.getBorrowId())) {
                DEMO_BORROWS.set(i, updated);
                return;
            }
        }
    }

    public static void removeDemoBorrow(String borrowId) {
        DEMO_BORROWS.removeIf(b -> borrowId.equals(b.getBorrowId()));
    }
}
