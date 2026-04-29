package jdbc.demo.dao;

import jdbc.demo.model.UserRecord;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    List<UserRecord> findAllUsers() throws SQLException;
    UserRecord findUserById(int userId) throws SQLException;
    void addUser(int userId, String firstName, String lastName,
                 String email, String contactNum, String type) throws SQLException;
    void updateUser(int userId, String firstName, String lastName,
                    String email, String contactNum, String type) throws SQLException;
    void deleteUser(int userId) throws SQLException;

    /** Returns the total number of borrow transactions on record for this user. */
    int getUserBorrowCount(int userId) throws SQLException;
}
