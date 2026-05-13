package jdbc.demo.model;

public class UserRecord {
    private final int userId;
    private String firstName;
    private String lastName;
    private final String email;
    private String contactNum;
    private final String type;
    private final String password;

    public UserRecord(int userId, String firstName, String lastName,
                      String email, String contactNum, String type, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNum = contactNum;
        this.type = type;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getContactNum() { return contactNum; }
    public String getType() { return type; }
    public String getPassword() { return password; }

    /** Updates display fields after a profile save (same row as login; keeps all panels in sync). */
    public void applyProfileFromForm(String firstName, String lastName, String contactNum) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNum = contactNum;
    }

    @Override
    public String toString() {
        return String.format(
                "UserID: %d | Name: %s %s | Email: %s | Contact: %s | Type: %s",
                userId, firstName, lastName, email, contactNum, type
        );
    }
}
