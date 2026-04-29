-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Apr 08, 2026 at 12:46 PM
-- Server version: 8.4.7
-- PHP Version: 8.3.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `kairosact4`
--

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `userId`      int             NOT NULL,
  `firstName`   varchar(20)     COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastName`    varchar(20)     COLLATE utf8mb4_unicode_ci NOT NULL,
  `email`       varchar(50)     COLLATE utf8mb4_unicode_ci NOT NULL,
  `contactnum`  varchar(20)     COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type`        varchar(9)      COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `email`      (`email`),
  UNIQUE KEY `contactnum` (`contactnum`),
  CONSTRAINT `user_type_chk` CHECK (`type` IN ('Student','Professor','Custodian'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`userId`, `firstName`, `lastName`, `email`, `contactnum`, `type`) VALUES
(2250010, 'Charlene',  'De Vera',   'cdv010@slu.edu.ph', '639492342341', 'Student'),
(2250020, 'Ash',       'Zulueta',   'az020@slu.edu.ph',  '639492342342', 'Student'),
(2250030, 'Shanaya',   'Bautista',  'sb030@slu.edu.ph',  '639492342343', 'Student'),
(2250040, 'Khirk',     'Longbuan',  'kl040@slu.edu.ph',  '639492342344', 'Student'),
(2250050, 'Mariane',   'Gobot',     'mg050@slu.edu.ph',  '639492342345', 'Student'),
(2250060, 'Aldine',    'Madriaga',  'am060@slu.edu.ph',  '639492342346', 'Student'),
(2250070, 'Sherlie',   'Rivera',    'sr070@slu.edu.ph',  '639492342347', 'Student'),
(2250080, 'Nathaniel', 'Ramos',     'nr080@slu.edu.ph',  '639492342348', 'Professor'),
(2250090, 'Sam',       'Villaflores','sv090@slu.edu.ph', '639492342349', 'Professor'),
(2250100, 'Abigail',   'Phillips',  'ap100@slu.edu.ph',  '639492342350', 'Professor'),
(2250110, 'Sarah',     'Geronimo',  'sg110@slu.edu.ph',  '639492342351', 'Custodian'),
(2250120, 'Pedro',     'Estrada',   'pe120@slu.edu.ph',  '639492342352', 'Professor'),
(2250130, 'Kenn',      'Sepulchre', 'ks130@slu.edu.ph',  '639492342353', 'Professor'),
(2250140, 'Gary',      'Valenciano','gv140@slu.edu.ph',  '639492342354', 'Custodian');

-- --------------------------------------------------------

--
-- Table structure for table `facility`
--

DROP TABLE IF EXISTS `facility`;
CREATE TABLE IF NOT EXISTS `facility` (
  `facilityId`   varchar(6)   COLLATE utf8mb4_unicode_ci NOT NULL,
  `facilityName` varchar(25)  COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`facilityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `facility`
--

INSERT INTO `facility` (`facilityId`, `facilityName`) VALUES
('F001', 'D528 Laboratory'),
('F002', 'D527 Laboratory'),
('F003', 'D526 Laboratory'),
('F004', 'Devesse AVR'),
('F005', 'Devesse Ampitheater'),
('F006', 'Devesse Lobby'),
('F007', 'D426 Open Laboratory'),
('F008', 'D424 Knowledge Center'),
('F009', 'Diocesan Parish Center'),
('F010', 'D512');

-- --------------------------------------------------------

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
CREATE TABLE IF NOT EXISTS `item` (
  `itemId`             varchar(15)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemName`           varchar(20)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemType`           varchar(10)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `description`        varchar(50)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `model`              varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `conditionStatus`    varchar(18)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `availabilityStatus` varchar(12)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateAcquired`       date         NOT NULL,
  PRIMARY KEY (`itemId`),
  CONSTRAINT `item_type_chk`         CHECK (`itemType`           IN ('tool','accessory','peripheral','equipment')),
  CONSTRAINT `item_cond_chk`         CHECK (`conditionStatus`    IN ('working','damaged','under maintenance')),
  CONSTRAINT `item_avail_chk`        CHECK (`availabilityStatus` IN ('available','borrowed','unavailable'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `item`
--

INSERT INTO `item` (`itemId`, `itemName`, `itemType`, `description`, `model`, `conditionStatus`, `availabilityStatus`, `dateAcquired`) VALUES
('I-CT-01',   'Crimping Tool',  'tool',       'Yellow Crimping Tool 1',              'Klein Tools VDV226 Crimp Tool for RJ12',                 'working',          'borrowed',    '2022-10-29'),
('I-EC-01',   'Ethernet Cable', 'accessory',  'Black Cat8 30-Foot Ethernet Cable 1', 'DbillionDa Cat8 Ethernet Cable 30Ft 26AWG',              'working',          'available',   '2022-12-23'),
('I-EC-02',   'Ethernet Cable', 'accessory',  'Black Cat8 30-Foot Ethernet Cable 2', 'DbillionDa Cat8 Ethernet Cable 30Ft 26AWG',              'damaged',          'unavailable', '2022-12-23'),
('I-HDMI-01', 'HDMI Cable',     'accessory',  'Black 4-Foot HDMI Cable 1',           'Philips 4-foot High-Speed HDMI Cable',                   'damaged',          'unavailable', '2022-02-01'),
('I-HDMI-02', 'HDMI Cable',     'accessory',  'Black 4-Foot HDMI Cable 2',           'Philips 4-foot High-Speed HDMI Cable',                   'working',          'available',   '2022-02-01'),
('I-HDMI-03', 'HDMI Cable',     'accessory',  'Black 4-Foot HDMI Cable 3',           'Philips 4-foot High-Speed HDMI Cable',                   'working',          'borrowed',    '2022-02-01'),
('I-KB-01',   'Keyboard',       'peripheral', 'Black Keyboard 1',                    'JLab - Epic Mechanical Advanced Multi-device Wireless Keyboard - Black', 'working', 'borrowed', '2020-06-28'),
('I-MO-01',   'Monitor',        'equipment',  'Silver 24-Inch Monitor 1',            'BenQ GW2486TC',                                          'working',          'available',   '2025-03-24'),
('I-PR-01',   'Projector',      'equipment',  'White LCD Projector 1',               'Epson PowerLite 119W LCD 4:3 Projector V11H985020',      'working',          'available',   '2022-01-22'),
('I-SD-01',   'Screwdriver',    'tool',       'Red Screwdriver 1',                   'RSPRO Phillips Standard Screwdriver',                    'working',          'borrowed',    '2022-10-29'),
('I-SD-02',   'Screwdriver',    'tool',       'Red Screwdriver 2',                   'RSPRO Phillips Standard Screwdriver',                    'working',          'available',   '2022-10-29'),
('I-SV-01',   'Server',         'equipment',  'Silver Server 1',                     'Cisco UCS B200 M5 Blade Server',                         'under maintenance','unavailable', '2021-05-09'),
('I-SV-02',   'Server',         'equipment',  'Silver Server 2',                     'Cisco UCS B200 M5 Blade Server',                         'working',          'available',   '2021-05-09'),
('I-SV-03',   'Server',         'equipment',  'Silver Server 3',                     'Cisco UCS B200 M5 Blade Server',                         'working',          'available',   '2021-05-09'),
('I-VGA-01',  'VGA Cable',      'accessory',  'Black 30M VGA Cable 1',               'RS PRO Male VGA to Male VGA Cable 30 m ',                'under maintenance','unavailable', '2022-01-22'),
('I-WM-01',   'Wired Mouse',    'peripheral', 'Black Wired Mouse 1',                 'LECOO MS101 Wired Mouse',                                'working',          'borrowed',    '2025-01-23'),
('I-WM-02',   'Wired Mouse',    'peripheral', 'Black Wired Mouse 2',                 'LECOO MS101 Wired Mouse',                                'damaged',          'unavailable', '2025-01-23'),
('I-WM-03',   'Wired Mouse',    'peripheral', 'Black Wired Mouse 3',                 'LECOO MS101 Wired Mouse',                                'working',          'available',   '2025-01-23');

-- --------------------------------------------------------

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
CREATE TABLE IF NOT EXISTS `activity` (
  `activityId`   varchar(6)   COLLATE utf8mb4_unicode_ci NOT NULL,
  `requesterId`  int          NOT NULL,
  `approvedBy`   int          DEFAULT NULL,
  `requestDate`  date         NOT NULL,
  `status`       varchar(9)   COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityName` varchar(50)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityType` varchar(25)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityDate` date         NOT NULL,
  `remarks`      varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`activityId`),
  KEY `act_c1` (`requesterId`),
  KEY `act_c2` (`approvedBy`),
  CONSTRAINT `act_status_chk` CHECK (`status` IN ('Pending','Approved','Rejected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `activity`
--

INSERT INTO `activity` (`activityId`, `requesterId`, `approvedBy`, `requestDate`, `status`, `activityName`, `activityType`, `activityDate`, `remarks`) VALUES
('A001', 2250010, 2250140, '2025-01-21', 'Approved', 'IT123 Activity',              'Class Activity',      '2025-01-22', 'return equipment immediately'),
('A002', 2250040, 2250140, '2025-01-22', 'Approved', 'IT213 Prelim Exam',           'Class Exam',          '2025-01-22', ''),
('A003', 2250050, 2250140, '2025-01-22', 'Approved', 'IT123 Activity',              'Class Activity',      '2025-01-22', ''),
('A004', 2250010, 2250140, '2025-01-22', 'Approved', 'IT212 Demonstration',         'Class Demonstration', '2025-01-22', ''),
('A005', 2250080, 2250110, '2025-01-20', 'Approved', 'Cryptocurrency Seminar',      'Seminar',             '2025-01-23', 'return equipment immediately'),
('A006', 2250060, 2250110, '2025-01-23', 'Approved', '3rd Year Recollection',       'Recollection',        '2025-01-23', ''),
('A007', 2250010, 2250110, '2025-01-23', 'Approved', 'Coding Seminar',              'Seminar',             '2025-01-23', ''),
('A008', 2250030, 2250140, '2025-01-25', 'Approved', 'Practice for Motherboard Exam','Practice',           '2025-01-25', ''),
('A009', 2250120, 2250140, '2025-01-28', 'Approved', 'ICON Meeting',                'Meeting',             '2025-01-28', ''),
('A010', 2250030, 2250140, '2025-01-29', 'Approved', 'Cloud Computing Seminar',     'Seminar',             '2025-01-29', ''),
('A011', 2250010, NULL,    '2025-01-22', 'Rejected', 'Hardware Seminar',            'Seminar',             '2025-01-22', 'unavailable equipment: I-SV-03'),
('A012', 2250080, 2250140, '2025-01-30', 'Approved', '2nd Year Recollection',       'Recollection',        '2026-04-20', '');

-- --------------------------------------------------------

--
-- Table structure for table `activitydetails`
--

DROP TABLE IF EXISTS `activitydetails`;
CREATE TABLE IF NOT EXISTS `activitydetails` (
  `activityId` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL,
  `facilityId` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`activityId`, `facilityId`),
  KEY `actdet_c2` (`facilityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `activitydetails`
--

INSERT INTO `activitydetails` (`activityId`, `facilityId`) VALUES
('A001', 'F001'),
('A002', 'F001'),
('A002', 'F002'),
('A003', 'F003'),
('A004', 'F001'),
('A004', 'F002'),
('A005', 'F006'),
('A006', 'F009'),
('A007', 'F004'),
('A008', 'F007'),
('A008', 'F008'),
('A009', 'F005'),
('A010', 'F004'),
('A011', 'F004'),
('A012', 'F009');

-- --------------------------------------------------------

--
-- Table structure for table `borrow`
--

DROP TABLE IF EXISTS `borrow`;
CREATE TABLE IF NOT EXISTS `borrow` (
  `borrowId`      varchar(6)   COLLATE utf8mb4_unicode_ci NOT NULL,
  `borrowerId`    int          NOT NULL,
  `custodianId`   int          NOT NULL,
  `dateBorrowed`  date         DEFAULT NULL,
  `timeBorrowed`  time         DEFAULT NULL,
  `activityId`    varchar(6)   COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateReturned`  date         DEFAULT NULL,
  `timeReturned`  time         DEFAULT NULL,
  `status`        varchar(30)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `remarks`       varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`borrowId`),
  KEY `borrow_c1` (`activityId`),
  KEY `borrow_c2` (`borrowerId`),
  KEY `borrow_c3` (`custodianId`),
  CONSTRAINT `borrow_status_chk` CHECK (`status` IN ('borrowed','returned','returned with damage','rejected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrow`
--
-- Note: B012 dateBorrowed/timeBorrowed are NULL because it has not been borrowed yet (reserved).

INSERT INTO `borrow` (`borrowId`, `borrowerId`, `custodianId`, `dateBorrowed`, `timeBorrowed`, `activityId`, `dateReturned`, `timeReturned`, `status`, `remarks`) VALUES
('B001', 2250010, 2250140, '2025-01-22', '10:00:00', 'A001', '2025-01-22', '11:00:00', 'returned',             ''),
('B002', 2250040, 2250140, '2025-01-22', '11:00:00', 'A002', '2025-01-22', '14:00:00', 'returned',             ''),
('B003', 2250050, 2250140, '2025-01-22', '12:00:00', 'A003', NULL,         NULL,        'borrowed',             ''),
('B004', 2250010, 2250140, '2025-01-22', '13:00:00', 'A004', '2025-01-23', '10:00:00', 'returned',             ''),
('B005', 2250080, 2250110, '2025-01-23', '09:00:00', 'A005', '2025-01-23', '09:30:00', 'returned',             ''),
('B006', 2250060, 2250110, '2025-01-23', '14:00:00', 'A006', '2025-01-23', '15:00:00', 'returned with damage', 'wired mouse not working'),
('B007', 2250010, 2250110, '2025-01-23', '14:00:00', 'A007', NULL,         NULL,        'borrowed',             ''),
('B008', 2250030, 2250140, '2025-01-25', '12:00:00', 'A008', NULL,         NULL,        'borrowed',             ''),
('B009', 2250120, 2250140, '2025-01-28', '16:00:00', 'A009', '2025-01-28', '16:30:00', 'returned with damage', 'ethernet cable returned with cuts, server returned with dent'),
('B010', 2250030, 2250140, '2025-01-29', '16:00:00', 'A010', '2025-01-30', '08:30:00', 'returned',             ''),
('B011', 2250010, 2250110, '2025-01-22', '17:00:00', 'A011', NULL,         NULL,        'rejected',             ''),
('B012', 2250080, 2250140, '2026-04-20', '08:00:00', 'A012', NULL,         NULL,        'borrowed',             'reserved for 4/20/26');

-- --------------------------------------------------------

--
-- Table structure for table `borrowdetails`
--

DROP TABLE IF EXISTS `borrowdetails`;
CREATE TABLE IF NOT EXISTS `borrowdetails` (
  `borrowId` varchar(6)  COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemId`   varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`borrowId`, `itemId`),
  KEY `bordet_c2` (`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrowdetails`
--

INSERT INTO `borrowdetails` (`borrowId`, `itemId`) VALUES
('B001', 'I-PR-01'),
('B001', 'I-WM-03'),
('B002', 'I-WM-03'),
('B003', 'I-WM-01'),
('B003', 'I-HDMI-03'),
('B004', 'I-SV-02'),
('B004', 'I-SV-03'),
('B004', 'I-SD-02'),
('B005', 'I-PR-01'),
('B006', 'I-WM-02'),
('B007', 'I-KB-01'),
('B007', 'I-CT-01'),
('B008', 'I-SD-01'),
('B009', 'I-EC-02'),
('B009', 'I-SV-01'),
('B010', 'I-HDMI-02'),
('B010', 'I-EC-01'),
('B011', 'I-SV-03'),
('B012', 'I-PR-01');

-- --------------------------------------------------------

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity`
--
ALTER TABLE `activity`
  ADD CONSTRAINT `act_c1` FOREIGN KEY (`requesterId`) REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `act_c2` FOREIGN KEY (`approvedBy`)  REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE;
-- Note: approvedBy is still nullable in DDL to allow Pending activities,
--       but the revised schema treats it as logically required once approved.

--
-- Constraints for table `activitydetails`
--
ALTER TABLE `activitydetails`
  ADD CONSTRAINT `actdet_c1` FOREIGN KEY (`activityId`) REFERENCES `activity` (`activityId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `actdet_c2` FOREIGN KEY (`facilityId`) REFERENCES `facility` (`facilityId`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `borrow`
--
ALTER TABLE `borrow`
  ADD CONSTRAINT `borrow_c1` FOREIGN KEY (`activityId`)  REFERENCES `activity` (`activityId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `borrow_c2` FOREIGN KEY (`borrowerId`)  REFERENCES `user` (`userId`)         ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `borrow_c3` FOREIGN KEY (`custodianId`) REFERENCES `user` (`userId`)         ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `borrowdetails`
--
ALTER TABLE `borrowdetails`
  ADD CONSTRAINT `bordet_c1` FOREIGN KEY (`borrowId`) REFERENCES `borrow` (`borrowId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bordet_c2` FOREIGN KEY (`itemId`)   REFERENCES `item`   (`itemId`)   ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DELIMITER $$

-- ------------------------------------------------------------
-- USER MANAGEMENT
-- ------------------------------------------------------------

-- Add a new user
DROP PROCEDURE IF EXISTS AddUser$$
CREATE PROCEDURE AddUser(
    IN p_userId     INT,
    IN p_firstName  VARCHAR(20),
    IN p_lastName   VARCHAR(20),
    IN p_email      VARCHAR(50),
    IN p_contactnum VARCHAR(20),
    IN p_type       VARCHAR(9)
)
BEGIN
    IF p_type NOT IN ('Student', 'Professor', 'Custodian') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid user type. Must be Student, Professor, or Custodian.';
    END IF;

    INSERT INTO `user` (userId, firstName, lastName, email, contactnum, type)
    VALUES (p_userId, p_firstName, p_lastName, p_email, p_contactnum, p_type);
END$$

-- Update an existing user's profile
DROP PROCEDURE IF EXISTS UpdateUser$$
CREATE PROCEDURE UpdateUser(
    IN p_userId     INT,
    IN p_firstName  VARCHAR(20),
    IN p_lastName   VARCHAR(20),
    IN p_email      VARCHAR(50),
    IN p_contactnum VARCHAR(20),
    IN p_type       VARCHAR(9)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_userId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'User not found.';
    END IF;

    IF p_type NOT IN ('Student', 'Professor', 'Custodian') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid user type. Must be Student, Professor, or Custodian.';
    END IF;

    UPDATE `user`
    SET firstName  = p_firstName,
        lastName   = p_lastName,
        email      = p_email,
        contactnum = p_contactnum,
        type       = p_type
    WHERE userId = p_userId;
END$$

-- Delete a user (cascades to activity / borrow via FK)
DROP PROCEDURE IF EXISTS DeleteUser$$
CREATE PROCEDURE DeleteUser(
    IN p_userId INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_userId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'User not found.';
    END IF;

    DELETE FROM `user` WHERE userId = p_userId;
END$$

-- ------------------------------------------------------------
-- FACILITY MANAGEMENT
-- ------------------------------------------------------------

-- Add a new facility
DROP PROCEDURE IF EXISTS AddFacility$$
CREATE PROCEDURE AddFacility(
    IN p_facilityId   VARCHAR(6),
    IN p_facilityName VARCHAR(25)
)
BEGIN
    IF EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility ID already exists.';
    END IF;

    INSERT INTO `facility` (facilityId, facilityName)
    VALUES (p_facilityId, p_facilityName);
END$$

-- Update a facility's name
DROP PROCEDURE IF EXISTS UpdateFacility$$
CREATE PROCEDURE UpdateFacility(
    IN p_facilityId   VARCHAR(6),
    IN p_facilityName VARCHAR(25)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility not found.';
    END IF;

    UPDATE `facility`
    SET facilityName = p_facilityName
    WHERE facilityId = p_facilityId;
END$$

-- Delete a facility (cascades to activitydetails via FK)
DROP PROCEDURE IF EXISTS DeleteFacility$$
CREATE PROCEDURE DeleteFacility(
    IN p_facilityId VARCHAR(6)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility not found.';
    END IF;

    DELETE FROM `facility` WHERE facilityId = p_facilityId;
END$$

-- ------------------------------------------------------------
-- ITEM MANAGEMENT
-- ------------------------------------------------------------

-- Add a new item to inventory
DROP PROCEDURE IF EXISTS AddItem$$
CREATE PROCEDURE AddItem(
    IN p_itemId             VARCHAR(15),
    IN p_itemName           VARCHAR(20),
    IN p_itemType           VARCHAR(10),
    IN p_description        VARCHAR(50),
    IN p_model              VARCHAR(100),
    IN p_conditionStatus    VARCHAR(18),
    IN p_availabilityStatus VARCHAR(12),
    IN p_dateAcquired       DATE
)
BEGIN
    IF p_itemType NOT IN ('tool','accessory','peripheral','equipment') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid itemType. Must be tool, accessory, peripheral, or equipment.';
    END IF;

    IF p_conditionStatus NOT IN ('working','damaged','under maintenance') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid conditionStatus. Must be working, damaged, or under maintenance.';
    END IF;

    IF p_availabilityStatus NOT IN ('available','borrowed','unavailable') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid availabilityStatus. Must be available, borrowed, or unavailable.';
    END IF;

    IF EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item ID already exists.';
    END IF;

    INSERT INTO `item` (itemId, itemName, itemType, description, model,
                        conditionStatus, availabilityStatus, dateAcquired)
    VALUES (p_itemId, p_itemName, p_itemType, p_description, p_model,
            p_conditionStatus, p_availabilityStatus, p_dateAcquired);
END$$

-- Update an item's editable fields (name, type, description, model, dateAcquired)
DROP PROCEDURE IF EXISTS UpdateItem$$
CREATE PROCEDURE UpdateItem(
    IN p_itemId      VARCHAR(15),
    IN p_itemName    VARCHAR(20),
    IN p_itemType    VARCHAR(10),
    IN p_description VARCHAR(50),
    IN p_model       VARCHAR(100),
    IN p_dateAcquired DATE
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    IF p_itemType NOT IN ('tool','accessory','peripheral','equipment') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid itemType.';
    END IF;

    UPDATE `item`
    SET itemName     = p_itemName,
        itemType     = p_itemType,
        description  = p_description,
        model        = p_model,
        dateAcquired = p_dateAcquired
    WHERE itemId = p_itemId;
END$$

-- Update only the status fields of an item (condition + availability)
DROP PROCEDURE IF EXISTS UpdateItemStatus$$
CREATE PROCEDURE UpdateItemStatus(
    IN p_itemId             VARCHAR(15),
    IN p_conditionStatus    VARCHAR(18),
    IN p_availabilityStatus VARCHAR(12)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    IF p_conditionStatus NOT IN ('working','damaged','under maintenance') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid conditionStatus.';
    END IF;

    IF p_availabilityStatus NOT IN ('available','borrowed','unavailable') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid availabilityStatus.';
    END IF;

    UPDATE `item`
    SET conditionStatus    = p_conditionStatus,
        availabilityStatus = p_availabilityStatus
    WHERE itemId = p_itemId;
END$$

-- Mark an item as under maintenance (sets conditionStatus and makes it unavailable)
DROP PROCEDURE IF EXISTS MarkItemUnderMaintenance$$
CREATE PROCEDURE MarkItemUnderMaintenance(
    IN p_itemId VARCHAR(15)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    UPDATE `item`
    SET conditionStatus    = 'under maintenance',
        availabilityStatus = 'unavailable'
    WHERE itemId = p_itemId;
END$$

-- Delete an item from inventory (cascades to borrowdetails via FK)
DROP PROCEDURE IF EXISTS DeleteItem$$
CREATE PROCEDURE DeleteItem(
    IN p_itemId VARCHAR(15)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    -- Prevent deletion if the item is currently borrowed
    IF EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId AND availabilityStatus = 'borrowed') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot delete an item that is currently borrowed.';
    END IF;

    DELETE FROM `item` WHERE itemId = p_itemId;
END$$

-- ------------------------------------------------------------
-- ACTIVITY MANAGEMENT
-- ------------------------------------------------------------

-- Submit a new activity request (status = Pending)
DROP PROCEDURE IF EXISTS SubmitActivityRequest$$
CREATE PROCEDURE SubmitActivityRequest(
    IN p_activityId   VARCHAR(6),
    IN p_requesterId  INT,
    IN p_requestDate  DATE,
    IN p_activityName VARCHAR(50),
    IN p_activityType VARCHAR(25),
    IN p_activityDate DATE,
    IN p_remarks      VARCHAR(100)
)
BEGIN
    IF EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity ID already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_requesterId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Requester not found.';
    END IF;

    IF p_activityDate < p_requestDate THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity date cannot be before request date.';
    END IF;

    INSERT INTO `activity` (activityId, requesterId, approvedBy, requestDate,
                            status, activityName, activityType, activityDate, remarks)
    VALUES (p_activityId, p_requesterId, NULL, p_requestDate,
            'Pending', p_activityName, p_activityType, p_activityDate,
            IFNULL(p_remarks, ''));
END$$

-- Add a facility to an activity's details
DROP PROCEDURE IF EXISTS AddFacilityToActivity$$
CREATE PROCEDURE AddFacilityToActivity(
    IN p_activityId VARCHAR(6),
    IN p_facilityId VARCHAR(6)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility not found.';
    END IF;

    IF EXISTS (SELECT 1 FROM `activitydetails`
               WHERE activityId = p_activityId AND facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility already linked to this activity.';
    END IF;

    INSERT INTO `activitydetails` (activityId, facilityId)
    VALUES (p_activityId, p_facilityId);
END$$

-- Remove a facility from an activity's details
DROP PROCEDURE IF EXISTS RemoveFacilityFromActivity$$
CREATE PROCEDURE RemoveFacilityFromActivity(
    IN p_activityId VARCHAR(6),
    IN p_facilityId VARCHAR(6)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `activitydetails`
                   WHERE activityId = p_activityId AND facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility is not linked to this activity.';
    END IF;

    DELETE FROM `activitydetails`
    WHERE activityId = p_activityId AND facilityId = p_facilityId;
END$$

-- Approve an activity request
DROP PROCEDURE IF EXISTS ApproveActivity$$
CREATE PROCEDURE ApproveActivity(
    IN p_activityId  VARCHAR(6),
    IN p_approvedBy  INT,
    IN p_remarks     VARCHAR(100)
)
BEGIN
    DECLARE v_status VARCHAR(9);

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    SELECT status INTO v_status FROM `activity` WHERE activityId = p_activityId;

    IF v_status != 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Pending activities can be approved.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_approvedBy AND type = 'Custodian') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Approver must be a Custodian.';
    END IF;

    UPDATE `activity`
    SET status     = 'Approved',
        approvedBy = p_approvedBy,
        remarks    = IFNULL(p_remarks, remarks)
    WHERE activityId = p_activityId;
END$$

-- Reject an activity request
DROP PROCEDURE IF EXISTS RejectActivity$$
CREATE PROCEDURE RejectActivity(
    IN p_activityId VARCHAR(6),
    IN p_approvedBy INT,
    IN p_remarks    VARCHAR(100)
)
BEGIN
    DECLARE v_status VARCHAR(9);

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    SELECT status INTO v_status FROM `activity` WHERE activityId = p_activityId;

    IF v_status != 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Pending activities can be rejected.';
    END IF;

    UPDATE `activity`
    SET status  = 'Rejected',
        remarks = IFNULL(p_remarks, remarks)
    WHERE activityId = p_activityId;
END$$

-- Update editable fields of an existing activity (only while Pending)
DROP PROCEDURE IF EXISTS UpdateActivity$$
CREATE PROCEDURE UpdateActivity(
    IN p_activityId   VARCHAR(6),
    IN p_activityName VARCHAR(50),
    IN p_activityType VARCHAR(25),
    IN p_activityDate DATE,
    IN p_remarks      VARCHAR(100)
)
BEGIN
    DECLARE v_status    VARCHAR(9);
    DECLARE v_reqDate   DATE;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    SELECT status, requestDate INTO v_status, v_reqDate
    FROM `activity` WHERE activityId = p_activityId;

    IF v_status != 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Pending activities can be edited.';
    END IF;

    IF p_activityDate < v_reqDate THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity date cannot be before the original request date.';
    END IF;

    UPDATE `activity`
    SET activityName = p_activityName,
        activityType = p_activityType,
        activityDate = p_activityDate,
        remarks      = IFNULL(p_remarks, remarks)
    WHERE activityId = p_activityId;
END$$

-- Delete an activity (cascades to activitydetails and borrow via FK)
DROP PROCEDURE IF EXISTS DeleteActivity$$
CREATE PROCEDURE DeleteActivity(
    IN p_activityId VARCHAR(6)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    DELETE FROM `activity` WHERE activityId = p_activityId;
END$$

-- ------------------------------------------------------------
-- BORROW MANAGEMENT
-- ------------------------------------------------------------

-- Create a borrow-out record (directly in 'borrowed' status)
DROP PROCEDURE IF EXISTS CreateBorrow$$
CREATE PROCEDURE CreateBorrow(
    IN p_borrowId     VARCHAR(6),
    IN p_borrowerId   INT,
    IN p_custodianId  INT,
    IN p_dateBorrowed DATE,
    IN p_timeBorrowed TIME,
    IN p_activityId   VARCHAR(6),
    IN p_remarks      VARCHAR(100)
)
BEGIN
    IF EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow ID already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_borrowerId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrower not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_custodianId AND type = 'Custodian') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Custodian not found or user is not a Custodian.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `activity`
                   WHERE activityId = p_activityId AND status = 'Approved') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found or not Approved.';
    END IF;

    INSERT INTO `borrow` (borrowId, borrowerId, custodianId, dateBorrowed,
                          timeBorrowed, activityId, dateReturned, timeReturned,
                          status, remarks)
    VALUES (p_borrowId, p_borrowerId, p_custodianId, p_dateBorrowed,
            p_timeBorrowed, p_activityId, NULL, NULL,
            'borrowed', IFNULL(p_remarks, ''));
END$$

-- Record a return (with optional damage flag)
DROP PROCEDURE IF EXISTS ReturnBorrow$$
CREATE PROCEDURE ReturnBorrow(
    IN p_borrowId     VARCHAR(6),
    IN p_dateReturned DATE,
    IN p_timeReturned TIME,
    IN p_withDamage   TINYINT(1),
    IN p_remarks      VARCHAR(100)
)
BEGIN
    DECLARE v_status VARCHAR(30);

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    SELECT status INTO v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_status != 'borrowed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only borrowed items can be returned.';
    END IF;

    UPDATE `borrow`
    SET dateReturned = p_dateReturned,
        timeReturned = p_timeReturned,
        status       = IF(p_withDamage, 'returned with damage', 'returned'),
        remarks      = IFNULL(p_remarks, remarks)
    WHERE borrowId = p_borrowId;

    -- Restore item availability; damaged items become unavailable
    UPDATE `item`
    SET availabilityStatus = IF(p_withDamage, 'unavailable', 'available'),
        conditionStatus    = IF(p_withDamage, 'damaged',     conditionStatus)
    WHERE itemId IN (
        SELECT itemId FROM `borrowdetails` WHERE borrowId = p_borrowId
    );
END$$

-- Add an item to an existing borrow record (only when status is 'to be borrowed')
DROP PROCEDURE IF EXISTS AddItemToBorrow$$
CREATE PROCEDURE AddItemToBorrow(
    IN p_borrowId VARCHAR(6),
    IN p_itemId   VARCHAR(15)
)
BEGIN
    DECLARE v_status VARCHAR(30);
    DECLARE v_avail  VARCHAR(12);

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    SELECT status INTO v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_status != 'borrowed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Items can only be added to active (borrowed) records.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    SELECT availabilityStatus INTO v_avail FROM `item` WHERE itemId = p_itemId;

    IF v_avail != 'available' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item is not available for borrowing.';
    END IF;

    IF EXISTS (SELECT 1 FROM `borrowdetails`
               WHERE borrowId = p_borrowId AND itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item is already in this borrow record.';
    END IF;

    INSERT INTO `borrowdetails` (borrowId, itemId)
    VALUES (p_borrowId, p_itemId);
END$$

-- Remove an item from a borrow record (only when status is 'to be borrowed')
DROP PROCEDURE IF EXISTS RemoveItemFromBorrow$$
CREATE PROCEDURE RemoveItemFromBorrow(
    IN p_borrowId VARCHAR(6),
    IN p_itemId   VARCHAR(15)
)
BEGIN
    DECLARE v_status VARCHAR(30);

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    SELECT status INTO v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_status != 'borrowed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Items can only be removed from active (borrowed) records.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `borrowdetails`
                   WHERE borrowId = p_borrowId AND itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item is not part of this borrow record.';
    END IF;

    DELETE FROM `borrowdetails`
    WHERE borrowId = p_borrowId AND itemId = p_itemId;
END$$

-- Update the custodian or remarks of a borrow record
DROP PROCEDURE IF EXISTS UpdateBorrow$$
CREATE PROCEDURE UpdateBorrow(
    IN p_borrowId    VARCHAR(6),
    IN p_custodianId INT,
    IN p_remarks     VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_custodianId AND type = 'Custodian') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Custodian not found or user is not a Custodian.';
    END IF;

    UPDATE `borrow`
    SET custodianId = p_custodianId,
        remarks     = IFNULL(p_remarks, remarks)
    WHERE borrowId = p_borrowId;
END$$

-- Delete a borrow record (cascades to borrowdetails via FK)
DROP PROCEDURE IF EXISTS DeleteBorrow$$
CREATE PROCEDURE DeleteBorrow(
    IN p_borrowId VARCHAR(6)
)
BEGIN
    DECLARE v_status VARCHAR(30);

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    SELECT status INTO v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_status IN ('borrowed') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot delete a borrow record while items are still borrowed. Return the items first.';
    END IF;

    DELETE FROM `borrow` WHERE borrowId = p_borrowId;
END$$

DELIMITER ;

-- ============================================================
-- STORED FUNCTIONS
-- ============================================================

DELIMITER $$

-- ------------------------------------------------------------
-- sf_item_availability_label
-- Returns a descriptive availability label for an item.
--   'Available'                      → item is free to borrow
--   'Borrowed by <BorrowerName>'     → currently checked out
--   'Unavailable (<conditionStatus>)'→ damaged or under maintenance
-- ------------------------------------------------------------
DROP FUNCTION IF EXISTS sf_item_availability_label$$
CREATE FUNCTION sf_item_availability_label(p_itemId VARCHAR(15))
RETURNS VARCHAR(80)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_avail  VARCHAR(12);
    DECLARE v_cond   VARCHAR(18);
    DECLARE v_name   VARCHAR(41);

    SELECT availabilityStatus, conditionStatus
    INTO   v_avail, v_cond
    FROM   item
    WHERE  itemId = p_itemId;

    IF v_avail IS NULL THEN
        RETURN 'Item not found';
    END IF;

    IF v_avail = 'available' THEN
        RETURN 'Available';
    END IF;

    IF v_avail = 'borrowed' THEN
        SELECT CONCAT(u.firstName, ' ', u.lastName)
        INTO   v_name
        FROM   borrow b
        JOIN   `user` u ON b.borrowerId = u.userId
        JOIN   borrowdetails bd ON b.borrowId = bd.borrowId
        WHERE  bd.itemId = p_itemId
          AND  b.status  = 'borrowed'
        ORDER BY b.dateBorrowed DESC
        LIMIT 1;

        RETURN CONCAT('Borrowed by ', IFNULL(v_name, 'Unknown'));
    END IF;

    -- unavailable
    RETURN CONCAT('Unavailable (', v_cond, ')');
END$$

-- ------------------------------------------------------------
-- sf_borrow_duration_days
-- Returns the number of days an item was/has been borrowed.
-- Uses dateReturned when available, otherwise CURDATE().
-- ------------------------------------------------------------
DROP FUNCTION IF EXISTS sf_borrow_duration_days$$
CREATE FUNCTION sf_borrow_duration_days(p_borrowId VARCHAR(6))
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_start  DATE;
    DECLARE v_end    DATE;

    SELECT dateBorrowed,
           COALESCE(dateReturned, CURDATE())
    INTO   v_start, v_end
    FROM   borrow
    WHERE  borrowId = p_borrowId;

    IF v_start IS NULL THEN
        RETURN -1;
    END IF;

    RETURN DATEDIFF(v_end, v_start);
END$$

-- ------------------------------------------------------------
-- sf_activity_status_label
-- Returns a combined status + approver label for an activity.
--   'Approved by <CustodianName>'
--   'Rejected (<remarks>)'
--   'Pending – awaiting approval'
-- ------------------------------------------------------------
DROP FUNCTION IF EXISTS sf_activity_status_label$$
CREATE FUNCTION sf_activity_status_label(p_activityId VARCHAR(6))
RETURNS VARCHAR(120)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_status  VARCHAR(9);
    DECLARE v_remarks VARCHAR(100);
    DECLARE v_name    VARCHAR(41);

    SELECT a.status, a.remarks,
           CONCAT(u.firstName, ' ', u.lastName)
    INTO   v_status, v_remarks, v_name
    FROM   activity a
    LEFT JOIN `user` u ON a.approvedBy = u.userId
    WHERE  a.activityId = p_activityId;

    IF v_status IS NULL THEN
        RETURN 'Activity not found';
    END IF;

    IF v_status = 'Approved' THEN
        RETURN CONCAT('Approved by ', IFNULL(v_name, 'N/A'));
    END IF;

    IF v_status = 'Rejected' THEN
        IF v_remarks IS NOT NULL AND TRIM(v_remarks) != '' THEN
            RETURN CONCAT('Rejected (', v_remarks, ')');
        END IF;
        RETURN 'Rejected';
    END IF;

    RETURN 'Pending - awaiting approval';
END$$

-- ------------------------------------------------------------
-- sf_user_borrow_count
-- Returns the total number of borrow transactions for a user
-- (regardless of status) as an integer.
-- ------------------------------------------------------------
DROP FUNCTION IF EXISTS sf_user_borrow_count$$
CREATE FUNCTION sf_user_borrow_count(p_userId INT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_count INT;

    SELECT COUNT(*)
    INTO   v_count
    FROM   borrow
    WHERE  borrowerId = p_userId;

    RETURN IFNULL(v_count, 0);
END$$

DELIMITER ;

-- ============================================================
-- EXAMPLE USAGE (commented out – uncomment to test manually)
-- ============================================================

-- -- Add a new student
-- CALL AddUser(2250150, 'Maria', 'Santos', 'ms150@slu.edu.ph', '639492342355', 'Student');

-- -- Update that student's contact number
-- CALL UpdateUser(2250150, 'Maria', 'Santos', 'ms150@slu.edu.ph', '639000000001', 'Student');

-- -- Add a new facility
-- CALL AddFacility('F011', 'D513 Conference Room');

-- -- Add a new item
-- CALL AddItem('I-LT-01', 'Laptop', 'equipment', 'Dell Latitude 5530 1', 'Dell Latitude 5530', 'working', 'available', '2024-06-01');

-- -- Mark an item under maintenance
-- CALL MarkItemUnderMaintenance('I-LT-01');

-- -- Restore item to working / available
-- CALL UpdateItemStatus('I-LT-01', 'working', 'available');

-- -- Submit a new activity request
-- CALL SubmitActivityRequest('A013', 2250020, CURDATE(), 'Network Lab Practice', 'Practice', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '');

-- -- Link a facility to the new activity
-- CALL AddFacilityToActivity('A013', 'F001');

-- -- Approve the activity
-- CALL ApproveActivity('A013', 2250140, '');

-- -- Create a borrow-out for the approved activity
-- CALL CreateBorrow('B013', 2250020, 2250140, CURDATE(), CURTIME(), 'A013', '');

-- -- Add more items to the active borrow
-- CALL AddItemToBorrow('B013', 'I-EC-01');
-- CALL AddItemToBorrow('B013', 'I-HDMI-02');

-- -- Return the items without damage
-- CALL ReturnBorrow('B013', CURDATE(), CURTIME(), 0, '');

-- -- Delete a user
-- CALL DeleteUser(2250150);

-- -- Stored function examples
-- -- Get availability label for an item
-- SELECT sf_item_availability_label('I-KB-01');          -- 'Borrowed by ...'
-- SELECT sf_item_availability_label('I-EC-01');          -- 'Available'
-- SELECT sf_item_availability_label('I-EC-02');          -- 'Unavailable (damaged)'

-- -- Get borrow duration in days for a borrow record
-- SELECT sf_borrow_duration_days('B001');                -- days between dates
-- SELECT sf_borrow_duration_days('B003');                -- days since borrowed (ongoing)

-- -- Get activity status label
-- SELECT sf_activity_status_label('A001');               -- 'Approved by Gary Valenciano'
-- SELECT sf_activity_status_label('A011');               -- 'Rejected (unavailable equipment: ...)'

-- -- Get total borrow count for a user
-- SELECT sf_user_borrow_count(2250010);                  -- number of borrows by user
-- SELECT itemId, sf_item_availability_label(itemId) AS availLabel FROM item ORDER BY itemId;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
