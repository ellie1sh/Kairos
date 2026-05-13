-- Fix: "Incorrect number of arguments for PROCEDURE ... AddUser; expected 6, got 7"
-- Run this once on the database you import from kairosact4_may11.sql (default: kairosact4_may11)
-- so AddUser accepts the generated password if your dump predates the 7-arg AddUser.
-- In MySQL client:  USE kairosact4_may11;   then source this file, or paste the block below.

DELIMITER $$

DROP PROCEDURE IF EXISTS AddUser$$
CREATE PROCEDURE AddUser(
    IN p_userId     INT,
    IN p_firstName  VARCHAR(20),
    IN p_lastName   VARCHAR(20),
    IN p_email      VARCHAR(50),
    IN p_contactnum VARCHAR(20),
    IN p_type       VARCHAR(9),
    IN p_password   VARCHAR(255)
)
BEGIN
    IF p_type NOT IN ('Student', 'Professor', 'Custodian', 'Admin') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid user type. Must be Student, Professor, Custodian, or Admin.';
    END IF;

    INSERT INTO `user` (userId, firstName, lastName, email, contactnum, type, password)
    VALUES (p_userId, p_firstName, p_lastName, p_email, p_contactnum, p_type, p_password);
END$$

DELIMITER ;
