
public class User {

    private int userID;
    private String userName;
    private String password;
    private String role;

    public User(int userID) {
        this.userID = userID;
    }

    void login(String userName, String role, String password) {
        this.userName = userName;
        this.password = password;
        this.role = role;
    }

    public int getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }

}
