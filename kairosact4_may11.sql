-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 11, 2026 at 05:15 AM
-- Server version: 9.1.0
-- PHP Version: 8.3.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE DATABASE IF NOT EXISTS `kairosact4_may11` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `kairosact4_may11`;

-- Collation utf8mb4_unicode_ci is used throughout for compatibility with MariaDB 10.4+ (MySQL 8 utf8mb4_0900_ai_ci is not available there).

-- Portable routines/views: no fixed DEFINER=root@localhost (definer is the account running this script).
-- Optional hardening: create an app user with SELECT + EXECUTE on procedures only (see bottom).

--
-- Database: `kairosact4_may11` 
--

DELIMITER $$
--
-- Procedures
--
DROP PROCEDURE IF EXISTS `AddFacility`$$
CREATE PROCEDURE `AddFacility` (IN `p_facilityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_facilityName` VARCHAR(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility ID already exists.';
    END IF;

    INSERT INTO `facility` (facilityId, facilityName)
    VALUES (p_facilityId, p_facilityName);
END$$

DROP PROCEDURE IF EXISTS `AddFacilityToActivity`$$
CREATE PROCEDURE `AddFacilityToActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_facilityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_requesterId INT;
    DECLARE v_status VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    SELECT requesterId, status INTO v_requesterId, v_status
    FROM `activity` WHERE activityId = p_activityId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_requesterId <> p_actorUserId OR v_status <> 'Pending' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only link facilities to their own pending activity requests.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the requester (borrower), Custodian, or Admin may link facilities.';
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

DROP PROCEDURE IF EXISTS `AddItem`$$
CREATE PROCEDURE `AddItem` (IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemType` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_description` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_model` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_conditionStatus` VARCHAR(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_availabilityStatus` VARCHAR(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_dateAcquired` DATE)   BEGIN
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

DROP PROCEDURE IF EXISTS `AddItemToBorrow`$$
CREATE PROCEDURE `AddItemToBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_status VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_avail  VARCHAR(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_borrowerId INT;
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;
    SELECT borrowerId, status INTO v_borrowerId, v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_borrowerId <> p_actorUserId THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only add items to their own borrow records.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the borrower on the record, Custodian, or Admin may add items.';
    END IF;

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

DROP PROCEDURE IF EXISTS `AddUser`$$
CREATE PROCEDURE `AddUser` (IN `p_userId` INT, IN `p_firstName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_lastName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_email` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_contactnum` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_type` VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF p_type NOT IN ('Student', 'Professor', 'Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid user type. Must be Student, Professor, Custodian, or Admin.';
    END IF;

    INSERT INTO `user` (userId, firstName, lastName, email, contactnum, type)
    VALUES (p_userId, p_firstName, p_lastName, p_email, p_contactnum, p_type);
END$$

DROP PROCEDURE IF EXISTS `ApproveActivity`$$
CREATE PROCEDURE `ApproveActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_approvedBy` INT, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    DECLARE v_status VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
                   WHERE userId = p_approvedBy AND type IN ('Custodian', 'Admin')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Approver must be a Custodian or Admin.';
    END IF;

    UPDATE `activity`
    SET status     = 'Approved',
        approvedBy = p_approvedBy,
        remarks    = IFNULL(p_remarks, remarks)
    WHERE activityId = p_activityId;
END$$

DROP PROCEDURE IF EXISTS `CreateBorrow`$$
CREATE PROCEDURE `CreateBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_borrowerId` INT, IN `p_custodianId` INT, IN `p_dateBorrowed` DATE, IN `p_timeBorrowed` TIME, IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF p_borrowerId <> p_actorUserId THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only create borrow records for themselves.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only a borrower (self), Custodian, or Admin may create a borrow.';
    END IF;

    IF EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow ID already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_borrowerId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrower not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_custodianId AND type IN ('Custodian', 'Admin')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Custodian not found or user is not a Custodian/Admin.';
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

DROP PROCEDURE IF EXISTS `DeleteActivity`$$
CREATE PROCEDURE `DeleteActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_requesterId INT;
    DECLARE v_status VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    SELECT requesterId, status INTO v_requesterId, v_status
    FROM `activity` WHERE activityId = p_activityId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_requesterId <> p_actorUserId OR v_status <> 'Pending' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only delete their own pending activity requests.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the requester (borrower), Custodian, or Admin may delete activities.';
    END IF;

    DELETE FROM `activity` WHERE activityId = p_activityId;
END$$

DROP PROCEDURE IF EXISTS `DeleteBorrow`$$
CREATE PROCEDURE `DeleteBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_status VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    IF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Custodian or Admin may delete borrow records.';
    END IF;

    SELECT status INTO v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_status IN ('borrowed') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot delete a borrow record while items are still borrowed. Return the items first.';
    END IF;

    DELETE FROM `borrow` WHERE borrowId = p_borrowId;
END$$

DROP PROCEDURE IF EXISTS `DeleteFacility`$$
CREATE PROCEDURE `DeleteFacility` (IN `p_facilityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF NOT EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility not found.';
    END IF;

    DELETE FROM `facility` WHERE facilityId = p_facilityId;
END$$

DROP PROCEDURE IF EXISTS `DeleteItem`$$
CREATE PROCEDURE `DeleteItem` (IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
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

DROP PROCEDURE IF EXISTS `DeleteUser`$$
CREATE PROCEDURE `DeleteUser` (IN `p_userId` INT)   BEGIN
    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_userId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'User not found.';
    END IF;

    DELETE FROM `user` WHERE userId = p_userId;
END$$

DROP PROCEDURE IF EXISTS `MarkItemUnderMaintenance`$$
CREATE PROCEDURE `MarkItemUnderMaintenance` (IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF NOT EXISTS (SELECT 1 FROM `item` WHERE itemId = p_itemId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Item not found.';
    END IF;

    UPDATE `item`
    SET conditionStatus    = 'under maintenance',
        availabilityStatus = 'unavailable'
    WHERE itemId = p_itemId;
END$$

DROP PROCEDURE IF EXISTS `RejectActivity`$$
CREATE PROCEDURE `RejectActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_approvedBy` INT, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    DECLARE v_status VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    SELECT status INTO v_status FROM `activity` WHERE activityId = p_activityId;

    IF v_status != 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Pending activities can be rejected.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_approvedBy AND type IN ('Custodian', 'Admin')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Rejecting user must be a Custodian or Admin.';
    END IF;

    UPDATE `activity`
    SET status  = 'Rejected',
        remarks = IFNULL(p_remarks, remarks)
    WHERE activityId = p_activityId;
END$$

DROP PROCEDURE IF EXISTS `RemoveFacilityFromActivity`$$
CREATE PROCEDURE `RemoveFacilityFromActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_facilityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_requesterId INT;
    DECLARE v_status VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    SELECT requesterId, status INTO v_requesterId, v_status
    FROM `activity` WHERE activityId = p_activityId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_requesterId <> p_actorUserId OR v_status <> 'Pending' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only change facilities on their own pending activity requests.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the requester (borrower), Custodian, or Admin may change facility links.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `activitydetails`
                   WHERE activityId = p_activityId AND facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility is not linked to this activity.';
    END IF;

    DELETE FROM `activitydetails`
    WHERE activityId = p_activityId AND facilityId = p_facilityId;
END$$

DROP PROCEDURE IF EXISTS `RemoveItemFromBorrow`$$
CREATE PROCEDURE `RemoveItemFromBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_status VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_borrowerId INT;
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;
    SELECT borrowerId, status INTO v_borrowerId, v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_borrowerId <> p_actorUserId THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only remove items from their own borrow records.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the borrower on the record, Custodian, or Admin may remove items.';
    END IF;

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

DROP PROCEDURE IF EXISTS `ReturnBorrow`$$
CREATE PROCEDURE `ReturnBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_dateReturned` DATE, IN `p_timeReturned` TIME, IN `p_withDamage` TINYINT(1), IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_status VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_borrowerId INT;
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;
    SELECT borrowerId, status INTO v_borrowerId, v_status FROM `borrow` WHERE borrowId = p_borrowId;

    IF v_actorType NOT IN ('Custodian', 'Admin')
       AND NOT (v_actorType IN ('Student', 'Professor') AND v_borrowerId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the borrower on the record, Custodian, or Admin may record a return.';
    END IF;

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

DROP PROCEDURE IF EXISTS `SubmitActivityRequest`$$
CREATE PROCEDURE `SubmitActivityRequest` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_requesterId` INT, IN `p_requestDate` DATE, IN `p_activityName` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_activityType` VARCHAR(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_activityDate` DATE, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity ID already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_requesterId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Requester not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_requesterId AND type IN ('Student', 'Professor')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity requests may only be submitted for Student or Professor accounts.';
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

DROP PROCEDURE IF EXISTS `UpdateActivity`$$
CREATE PROCEDURE `UpdateActivity` (IN `p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_activityName` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_activityType` VARCHAR(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_activityDate` DATE, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_status      VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_reqDate     DATE;
    DECLARE v_requesterId INT;
    DECLARE v_actorType   VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `activity` WHERE activityId = p_activityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Activity not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    SELECT status, requestDate, requesterId INTO v_status, v_reqDate, v_requesterId
    FROM `activity` WHERE activityId = p_activityId;

    IF v_status != 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Pending activities can be edited.';
    END IF;

    IF v_actorType IN ('Student', 'Professor') THEN
        IF v_requesterId <> p_actorUserId THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Borrowers may only update their own activity requests.';
        END IF;
    ELSEIF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the requester (borrower), Custodian, or Admin may update activities.';
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

DROP PROCEDURE IF EXISTS `UpdateBorrow`$$
CREATE PROCEDURE `UpdateBorrow` (IN `p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_custodianId` INT, IN `p_remarks` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_actorUserId` INT)   BEGIN
    DECLARE v_actorType VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    IF NOT EXISTS (SELECT 1 FROM `borrow` WHERE borrowId = p_borrowId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Borrow record not found.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_actorUserId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Actor user not found.';
    END IF;

    SELECT type INTO v_actorType FROM `user` WHERE userId = p_actorUserId;

    IF v_actorType NOT IN ('Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only Custodian or Admin may update borrow records.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM `user`
                   WHERE userId = p_custodianId AND type IN ('Custodian', 'Admin')) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Custodian not found or user is not a Custodian/Admin.';
    END IF;

    UPDATE `borrow`
    SET custodianId = p_custodianId,
        remarks     = IFNULL(p_remarks, remarks)
    WHERE borrowId = p_borrowId;
END$$

DROP PROCEDURE IF EXISTS `UpdateFacility`$$
CREATE PROCEDURE `UpdateFacility` (IN `p_facilityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_facilityName` VARCHAR(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF NOT EXISTS (SELECT 1 FROM `facility` WHERE facilityId = p_facilityId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Facility not found.';
    END IF;

    UPDATE `facility`
    SET facilityName = p_facilityName
    WHERE facilityId = p_facilityId;
END$$

DROP PROCEDURE IF EXISTS `UpdateItem`$$
CREATE PROCEDURE `UpdateItem` (IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_itemType` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_description` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_model` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_dateAcquired` DATE)   BEGIN
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

DROP PROCEDURE IF EXISTS `UpdateItemStatus`$$
CREATE PROCEDURE `UpdateItemStatus` (IN `p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_conditionStatus` VARCHAR(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_availabilityStatus` VARCHAR(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
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

DROP PROCEDURE IF EXISTS `UpdateUser`$$
CREATE PROCEDURE `UpdateUser` (IN `p_userId` INT, IN `p_firstName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_lastName` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_email` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_contactnum` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci, IN `p_type` VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci)   BEGIN
    IF NOT EXISTS (SELECT 1 FROM `user` WHERE userId = p_userId) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'User not found.';
    END IF;

    IF p_type NOT IN ('Student', 'Professor', 'Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid user type. Must be Student, Professor, Custodian, or Admin.';
    END IF;

    UPDATE `user`
    SET firstName  = p_firstName,
        lastName   = p_lastName,
        email      = p_email,
        contactnum = p_contactnum,
        type       = p_type
    WHERE userId = p_userId;
END$$

--
-- Functions
--
DROP FUNCTION IF EXISTS `sf_activity_status_label`$$
CREATE FUNCTION `sf_activity_status_label` (`p_activityId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci) RETURNS VARCHAR(120) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci DETERMINISTIC READS SQL DATA BEGIN
    DECLARE v_status  VARCHAR(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_remarks VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_name    VARCHAR(41) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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

DROP FUNCTION IF EXISTS `sf_borrow_duration_days`$$
CREATE FUNCTION `sf_borrow_duration_days` (`p_borrowId` VARCHAR(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci) RETURNS INT DETERMINISTIC READS SQL DATA BEGIN
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

DROP FUNCTION IF EXISTS `sf_item_availability_label`$$
CREATE FUNCTION `sf_item_availability_label` (`p_itemId` VARCHAR(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci) RETURNS VARCHAR(80) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci DETERMINISTIC READS SQL DATA BEGIN
    DECLARE v_avail  VARCHAR(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_cond   VARCHAR(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE v_name   VARCHAR(41) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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

DROP FUNCTION IF EXISTS `sf_user_borrow_count`$$
CREATE FUNCTION `sf_user_borrow_count` (`p_userId` INT) RETURNS INT DETERMINISTIC READS SQL DATA BEGIN
    DECLARE v_count INT;

    SELECT COUNT(*)
    INTO   v_count
    FROM   borrow
    WHERE  borrowerId = p_userId;

    RETURN IFNULL(v_count, 0);
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
CREATE TABLE IF NOT EXISTS `activity` (
  `activityId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `requesterId` int NOT NULL,
  `approvedBy` int DEFAULT NULL,
  `requestDate` date NOT NULL,
  `status` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityType` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activityDate` date NOT NULL,
  `remarks` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`activityId`),
  KEY `act_c1` (`requesterId`),
  KEY `act_c2` (`approvedBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `activity`
--

INSERT INTO `activity` (`activityId`, `requesterId`, `approvedBy`, `requestDate`, `status`, `activityName`, `activityType`, `activityDate`, `remarks`) VALUES
('A001', 2250010, 2250140, '2025-01-21', 'Approved', 'IT123 Activity', 'Class Activity', '2025-01-22', 'return equipment immediately'),
('A002', 2250040, 2250140, '2025-01-22', 'Approved', 'IT213 Prelim Exam', 'Class Exam', '2025-01-22', ''),
('A003', 2250050, 2250140, '2025-01-22', 'Approved', 'IT123 Activity', 'Class Activity', '2025-01-22', ''),
('A004', 2250010, 2250140, '2025-01-22', 'Approved', 'IT212 Demonstration', 'Class Demonstration', '2025-01-22', ''),
('A005', 2250080, 2250110, '2025-01-20', 'Approved', 'Cryptocurrency Seminar', 'Seminar', '2025-01-23', 'return equipment immediately'),
('A006', 2250060, 2250110, '2025-01-23', 'Approved', '3rd Year Recollection', 'Recollection', '2025-01-23', ''),
('A007', 2250010, 2250110, '2025-01-23', 'Approved', 'Coding Seminar', 'Seminar', '2025-01-23', ''),
('A008', 2250030, 2250140, '2025-01-25', 'Approved', 'Practice for Motherboard Exam', 'Practice', '2025-01-25', ''),
('A009', 2250120, 2250140, '2025-01-28', 'Approved', 'ICON Meeting', 'Meeting', '2025-01-28', ''),
('A010', 2250030, 2250140, '2025-01-29', 'Approved', 'Cloud Computing Seminar', 'Seminar', '2025-01-29', ''),
('A011', 2250010, NULL, '2025-01-22', 'Rejected', 'Hardware Seminar', 'Seminar', '2025-01-22', 'unavailable equipment: I-SV-03'),
('A012', 2250060, 2250140, '2025-01-31', 'Approved', 'IT123 Activity', 'Class Activity', '2025-02-01', ''),
('A013', 2250120, 2250140, '2025-02-01', 'Approved', 'IT123 Activity', 'Class Activity', '2025-02-02', '');

-- --------------------------------------------------------

--
-- Table structure for table `activitydetails`
--

DROP TABLE IF EXISTS `activitydetails`;
CREATE TABLE IF NOT EXISTS `activitydetails` (
  `activityId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `facilityId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`activityId`,`facilityId`),
  KEY `actdet_c2` (`facilityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `activitydetails`
--

INSERT INTO `activitydetails` (`activityId`, `facilityId`) VALUES
('A001', 'F001'),
('A002', 'F001'),
('A004', 'F001'),
('A012', 'F001'),
('A013', 'F001'),
('A002', 'F002'),
('A004', 'F002'),
('A003', 'F003'),
('A007', 'F004'),
('A010', 'F004'),
('A011', 'F004'),
('A009', 'F005'),
('A005', 'F006'),
('A008', 'F007'),
('A008', 'F008'),
('A006', 'F009');

-- --------------------------------------------------------

--
-- Table structure for table `borrow`
--

DROP TABLE IF EXISTS `borrow`;
CREATE TABLE IF NOT EXISTS `borrow` (
  `borrowId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `borrowerId` int NOT NULL,
  `custodianId` int NOT NULL,
  `dateBorrowed` date DEFAULT NULL,
  `timeBorrowed` time DEFAULT NULL,
  `activityId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateReturned` date DEFAULT NULL,
  `timeReturned` time DEFAULT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `remarks` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`borrowId`),
  KEY `borrow_c1` (`activityId`),
  KEY `borrow_c2` (`borrowerId`),
  KEY `borrow_c3` (`custodianId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrow`
--

INSERT INTO `borrow` (`borrowId`, `borrowerId`, `custodianId`, `dateBorrowed`, `timeBorrowed`, `activityId`, `dateReturned`, `timeReturned`, `status`, `remarks`) VALUES
('B001', 2250010, 2250140, '2025-01-22', '10:00:00', 'A001', '2025-01-22', '11:00:00', 'returned', ''),
('B002', 2250040, 2250140, '2025-01-22', '11:00:00', 'A002', '2025-01-22', '14:00:00', 'returned', ''),
('B003', 2250050, 2250140, '2025-01-22', '12:00:00', 'A003', NULL, NULL, 'borrowed', ''),
('B004', 2250010, 2250140, '2025-01-22', '13:00:00', 'A004', '2025-01-23', '10:00:00', 'returned', ''),
('B005', 2250080, 2250110, '2025-01-23', '09:00:00', 'A005', '2025-01-23', '09:30:00', 'returned', ''),
('B006', 2250060, 2250110, '2025-01-23', '14:00:00', 'A006', '2025-01-23', '15:00:00', 'returned with damage', 'wired mouse not working'),
('B007', 2250010, 2250110, '2025-01-23', '14:00:00', 'A007', NULL, NULL, 'borrowed', ''),
('B008', 2250030, 2250140, '2025-01-25', '12:00:00', 'A008', NULL, NULL, 'borrowed', ''),
('B009', 2250120, 2250140, '2025-01-28', '16:00:00', 'A009', '2025-01-28', '16:30:00', 'returned with damage', 'ethernet cable returned with cuts, server returned with dent'),
('B010', 2250030, 2250140, '2025-01-29', '16:00:00', 'A010', '2025-01-30', '08:30:00', 'returned', ''),
('B011', 2250010, 2250110, '2025-01-22', '17:00:00', 'A011', NULL, NULL, 'rejected', ''),
('B012', 2250060, 2250140, '2025-02-01', '09:00:00', 'A012', '2025-02-01', '11:00:00', 'returned', ''),
('B013', 2250120, 2250140, '2025-02-02', '14:00:00', 'A013', '2025-02-02', '16:00:00', 'returned', ''),
('B020', 2250010, 2250110, '2026-05-11', '13:11:07', 'A009', NULL, NULL, 'borrowed', '');

-- --------------------------------------------------------

--
-- Table structure for table `borrowdetails`
--

DROP TABLE IF EXISTS `borrowdetails`;
CREATE TABLE IF NOT EXISTS `borrowdetails` (
  `borrowId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemId` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`borrowId`,`itemId`),
  KEY `bordet_c2` (`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrowdetails`
--

INSERT INTO `borrowdetails` (`borrowId`, `itemId`) VALUES
('B007', 'I-CT-01'),
('B010', 'I-EC-01'),
('B009', 'I-EC-02'),
('B010', 'I-HDMI-02'),
('B003', 'I-HDMI-03'),
('B007', 'I-KB-01'),
('B001', 'I-PR-01'),
('B005', 'I-PR-01'),
('B012', 'I-PR-01'),
('B020', 'I-PR-01'),
('B008', 'I-SD-01'),
('B004', 'I-SD-02'),
('B009', 'I-SV-01'),
('B004', 'I-SV-02'),
('B004', 'I-SV-03'),
('B011', 'I-SV-03'),
('B013', 'I-SV-03'),
('B003', 'I-WM-01'),
('B006', 'I-WM-02'),
('B001', 'I-WM-03'),
('B002', 'I-WM-03');

-- --------------------------------------------------------

--
-- Table structure for table `facility`
--

DROP TABLE IF EXISTS `facility`;
CREATE TABLE IF NOT EXISTS `facility` (
  `facilityId` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `facilityName` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
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
('F005', 'Devesse Amphitheater'),
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
  `itemId` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemName` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `conditionStatus` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `availabilityStatus` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateAcquired` date NOT NULL,
  PRIMARY KEY (`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `item`
--

INSERT INTO `item` (`itemId`, `itemName`, `itemType`, `description`, `model`, `conditionStatus`, `availabilityStatus`, `dateAcquired`) VALUES
('I-CT-01', 'Crimping Tool', 'tool', 'Yellow Crimping Tool 1', 'Klein Tools VDV226 Crimp Tool for RJ12', 'working', 'borrowed', '2022-10-29'),
('I-EC-01', 'Ethernet Cable', 'accessory', 'Black Cat8 30-Foot Ethernet Cable 1', 'DbillionDa Cat8 Ethernet Cable 30Ft 26AWG', 'working', 'available', '2022-12-23'),
('I-EC-02', 'Ethernet Cable', 'accessory', 'Black Cat8 30-Foot Ethernet Cable 2', 'DbillionDa Cat8 Ethernet Cable 30Ft 26AWG', 'damaged', 'unavailable', '2022-12-23'),
('I-HDMI-01', 'HDMI Cable', 'accessory', 'Black 4-Foot HDMI Cable 1', 'Philips 4-foot High-Speed HDMI Cable', 'damaged', 'unavailable', '2022-02-01'),
('I-HDMI-02', 'HDMI Cable', 'accessory', 'Black 4-Foot HDMI Cable 2', 'Philips 4-foot High-Speed HDMI Cable', 'working', 'available', '2022-02-01'),
('I-HDMI-03', 'HDMI Cable', 'accessory', 'Black 4-Foot HDMI Cable 3', 'Philips 4-foot High-Speed HDMI Cable', 'working', 'borrowed', '2022-02-01'),
('I-KB-01', 'Keyboard', 'peripheral', 'Black Keyboard 1', 'JLab - Epic Mechanical Advanced Multi-device Wireless Keyboard - Black', 'working', 'borrowed', '2020-06-28'),
('I-MO-01', 'Monitor', 'equipment', 'Silver 24-Inch Monitor 1', 'BenQ GW2486TC', 'working', 'available', '2025-03-24'),
('I-PR-01', 'Projector', 'equipment', 'White LCD Projector 1', 'Epson PowerLite 119W LCD 4:3 Projector V11H985020', 'working', 'available', '2022-01-22'),
('I-SD-01', 'Screwdriver', 'tool', 'Red Screwdriver 1', 'RSPRO Phillips Standard Screwdriver', 'working', 'borrowed', '2022-10-29'),
('I-SD-02', 'Screwdriver', 'tool', 'Red Screwdriver 2', 'RSPRO Phillips Standard Screwdriver', 'working', 'available', '2022-10-29'),
('I-SV-01', 'Server', 'equipment', 'Silver Server 1', 'Cisco UCS B200 M5 Blade Server', 'under maintenance', 'unavailable', '2021-05-09'),
('I-SV-02', 'Server', 'equipment', 'Silver Server 2', 'Cisco UCS B200 M5 Blade Server', 'working', 'available', '2021-05-09'),
('I-SV-03', 'Server', 'equipment', 'Silver Server 3', 'Cisco UCS B200 M5 Blade Server', 'working', 'available', '2021-05-09'),
('I-VGA-01', 'VGA Cable', 'accessory', 'Black 30M VGA Cable 1', 'RS PRO Male VGA to Male VGA Cable 30 m ', 'under maintenance', 'unavailable', '2022-01-22'),
('I-WM-01', 'Wired Mouse', 'peripheral', 'Black Wired Mouse 1', 'LECOO MS101 Wired Mouse', 'working', 'borrowed', '2025-01-23'),
('I-WM-02', 'Wired Mouse', 'peripheral', 'Black Wired Mouse 2', 'LECOO MS101 Wired Mouse', 'damaged', 'unavailable', '2025-01-23'),
('I-WM-03', 'Wired Mouse', 'peripheral', 'Black Wired Mouse 3', 'LECOO MS101 Wired Mouse', 'working', 'available', '2025-01-23');

-- --------------------------------------------------------

--
-- Table structure for table `maintenance_log_items`
--

DROP TABLE IF EXISTS `maintenance_log_items`;
CREATE TABLE IF NOT EXISTS `maintenance_log_items` (
  `maintenanceId` int NOT NULL AUTO_INCREMENT,
  `itemId` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemName` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `itemType` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `conditionStatus` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `availabilityStatus` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`maintenanceId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `maintenance_log_items`
--

INSERT INTO `maintenance_log_items` (`maintenanceId`, `itemId`, `itemName`, `itemType`, `conditionStatus`, `availabilityStatus`) VALUES
(1, 'I-EC-02', 'Ethernet Cable', 'accessory', 'damaged', 'unavailable'),
(2, 'I-HDMI-01', 'HDMI Cable', 'accessory', 'damaged', 'unavailable'),
(3, 'I-SV-01', 'Server', 'equipment', 'under maintenance', 'unavailable'),
(4, 'I-VGA-01', 'VGA Cable', 'accessory', 'under maintenance', 'unavailable'),
(5, 'I-WM-02', 'Wired Mouse', 'peripheral', 'damaged', 'unavailable'),
(6, 'I-PR-01', 'Projector', 'equipment', 'working', 'available'),
(7, 'I-SV-03', 'Server', 'equipment', 'working', 'available');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `userId` int NOT NULL,
  `firstName` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `lastName` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contactnum` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`userId`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `contactnum` (`contactnum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`userId`, `firstName`, `lastName`, `email`, `contactnum`, `type`, `password`) VALUES
(1000000, 'Admin', 'Kairos', 'admin@kairos.edu', '639000000000', 'Admin', 'admin123'),
(2250010, 'Charlene', 'De Vera', 'cdv010@slu.edu.ph', '639492342341', 'Student', 'stud123'),
(2250020, 'Ash', 'Zulueta', 'az020@slu.edu.ph', '639492342342', 'Student', 'stud123'),
(2250030, 'Shanaya', 'Bautista', 'sb030@slu.edu.ph', '639492342343', 'Student', 'stud123'),
(2250040, 'Khirk', 'Longbuan', 'kl040@slu.edu.ph', '639492342344', 'Student', 'stud123'),
(2250050, 'Mariane', 'Gobot', 'mg050@slu.edu.ph', '639492342345', 'Student', 'stud123'),
(2250060, 'Aldine', 'Madriaga', 'am060@slu.edu.ph', '639492342346', 'Student', 'stud123'),
(2250070, 'Sherlie', 'Rivera', 'sr070@slu.edu.ph', '639492342347', 'Student', 'stud123'),
(2250080, 'Nathaniel', 'Ramos', 'nr080@slu.edu.ph', '639492342348', 'Professor', 'prof123'),
(2250090, 'Sam', 'Villaflores', 'sv090@slu.edu.ph', '639492342349', 'Professor', 'prof123'),
(2250100, 'Abigail', 'Phillips', 'ap100@slu.edu.ph', '639492342350', 'Professor', 'prof123'),
(2250110, 'Sarah', 'Geronimo', 'sg110@slu.edu.ph', '639492342351', 'Custodian', 'cust123'),
(2250120, 'Pedro', 'Estrada', 'pe120@slu.edu.ph', '639492342352', 'Professor', 'prof123'),
(2250130, 'Kenn', 'Sepulchre', 'ks130@slu.edu.ph', '639492342353', 'Professor', 'prof123'),
(2250140, 'Gary', 'Valenciano', 'gv140@slu.edu.ph', '639492342354', 'Custodian', 'cust123');

-- --------------------------------------------------------

--
-- Stand-in structure for view `vw_approved_activity_summary`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `vw_approved_activity_summary`;
CREATE TABLE IF NOT EXISTS `vw_approved_activity_summary` (
`ActivityDate` date
,`ActivityID` varchar(6)
,`ActivityName` varchar(50)
,`ActivityType` varchar(25)
,`ApprovedBy` varchar(41)
,`Requester` varchar(41)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vw_borrower_activity_log`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `vw_borrower_activity_log`;
CREATE TABLE IF NOT EXISTS `vw_borrower_activity_log` (
`ActivityName` varchar(50)
,`Borrower` varchar(41)
,`BorrowID` varchar(6)
,`BorrowStatus` varchar(30)
,`DateBorrowed` date
,`DateReturned` date
,`Remarks` varchar(100)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vw_borrow_status_per_item`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `vw_borrow_status_per_item`;
CREATE TABLE IF NOT EXISTS `vw_borrow_status_per_item` (
`AvailabilityStatus` varchar(12)
,`ConditionStatus` varchar(18)
,`ItemID` varchar(15)
,`ItemName` varchar(20)
,`ItemType` varchar(10)
,`TotalTimesBorrowed` bigint
);

-- --------------------------------------------------------

--
-- Structure for view `vw_approved_activity_summary`
--
DROP TABLE IF EXISTS `vw_approved_activity_summary`;

DROP VIEW IF EXISTS `vw_approved_activity_summary`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY INVOKER VIEW `vw_approved_activity_summary`  AS SELECT `a`.`activityId` AS `ActivityID`, `a`.`activityName` AS `ActivityName`, `a`.`activityType` AS `ActivityType`, `a`.`activityDate` AS `ActivityDate`, concat(`req`.`firstName`,' ',`req`.`lastName`) AS `Requester`, concat(`apr`.`firstName`,' ',`apr`.`lastName`) AS `ApprovedBy` FROM ((`activity` `a` join `user` `req` on((`a`.`requesterId` = `req`.`userId`))) join `user` `apr` on((`a`.`approvedBy` = `apr`.`userId`))) WHERE ((`a`.`status` = 'Approved') AND (`a`.`activityDate` < curdate())) ;

-- --------------------------------------------------------

--
-- Structure for view `vw_borrower_activity_log`
--
DROP TABLE IF EXISTS `vw_borrower_activity_log`;

DROP VIEW IF EXISTS `vw_borrower_activity_log`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY INVOKER VIEW `vw_borrower_activity_log`  AS SELECT `b`.`borrowId` AS `BorrowID`, concat(`u`.`firstName`,' ',`u`.`lastName`) AS `Borrower`, `a`.`activityName` AS `ActivityName`, `b`.`dateBorrowed` AS `DateBorrowed`, `b`.`dateReturned` AS `DateReturned`, `b`.`status` AS `BorrowStatus`, `b`.`remarks` AS `Remarks` FROM ((`borrow` `b` join `user` `u` on((`b`.`borrowerId` = `u`.`userId`))) join `activity` `a` on((`b`.`activityId` = `a`.`activityId`))) WHERE (`b`.`status` in ('returned','returned with damage')) ;

-- --------------------------------------------------------

--
-- Structure for view `vw_borrow_status_per_item`
--
DROP TABLE IF EXISTS `vw_borrow_status_per_item`;

DROP VIEW IF EXISTS `vw_borrow_status_per_item`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY INVOKER VIEW `vw_borrow_status_per_item`  AS SELECT `i`.`itemId` AS `ItemID`, `i`.`itemName` AS `ItemName`, `i`.`itemType` AS `ItemType`, `i`.`conditionStatus` AS `ConditionStatus`, `i`.`availabilityStatus` AS `AvailabilityStatus`, count(`bd`.`borrowId`) AS `TotalTimesBorrowed` FROM ((`item` `i` left join `borrowdetails` `bd` on((`i`.`itemId` = `bd`.`itemId`))) left join `borrow` `b` on((`bd`.`borrowId` = `b`.`borrowId`))) GROUP BY `i`.`itemId`, `i`.`itemName`, `i`.`itemType`, `i`.`conditionStatus`, `i`.`availabilityStatus` ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity`
--
ALTER TABLE `activity`
  ADD CONSTRAINT `act_c1` FOREIGN KEY (`requesterId`) REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `act_c2` FOREIGN KEY (`approvedBy`) REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE;

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
  ADD CONSTRAINT `borrow_c1` FOREIGN KEY (`activityId`) REFERENCES `activity` (`activityId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `borrow_c2` FOREIGN KEY (`borrowerId`) REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `borrow_c3` FOREIGN KEY (`custodianId`) REFERENCES `user` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `borrowdetails`
--
ALTER TABLE `borrowdetails`
  ADD CONSTRAINT `bordet_c1` FOREIGN KEY (`borrowId`) REFERENCES `borrow` (`borrowId`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `bordet_c2` FOREIGN KEY (`itemId`) REFERENCES `item` (`itemId`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `maintenance_log_items`
--
ALTER TABLE `maintenance_log_items`
  ADD CONSTRAINT `maint_log_item_fk` FOREIGN KEY (`itemId`) REFERENCES `item` (`itemId`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Optional least-privilege application user (run manually after import).
--   CREATE USER IF NOT EXISTS 'kairos_app'@'localhost' IDENTIFIED BY 'change_me';
--   GRANT SELECT ON `kairosact4_may11`.* TO 'kairos_app'@'localhost';
--   GRANT EXECUTE ON `kairosact4_may11`.* TO 'kairos_app'@'localhost';
--   FLUSH PRIVILEGES;
-- Then point the JDBC URL at this user so direct DML on business tables can be revoked if desired.
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
