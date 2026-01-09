package ci553.happyshop.authentication;

public class UserAccount {
    private final String username;
    private final UserRole role;

    public UserAccount(String username,UserRole role) {
        this.username = username;
        this.role = role;
    }
    public String getUsername() {return username;}
    public UserRole getRole() {return role;}
}
