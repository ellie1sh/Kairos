package jdbc.demo.model;

public class UserRecord {
    private final int userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String contactNum;
    private final String type;

    public UserRecord(int userId, String firstName, String lastName,
                      String email, String contactNum, String type) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNum = contactNum;
        this.type = type;
    }

    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getContactNum() { return contactNum; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return String.format(
                "UserID: %d | Name: %s %s | Email: %s | Contact: %s | Type: %s",
                userId, firstName, lastName, email, contactNum, type
        );
    }
}
