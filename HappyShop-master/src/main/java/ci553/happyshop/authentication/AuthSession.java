package ci553.happyshop.authentication;

public final class AuthSession {
    private static UserAccount currentUser;

    private AuthSession() {}

    public static void set(UserAccount user) {currentUser = user;}
    public static UserAccount get()  {return currentUser;}
    public static void clear() {currentUser = null;}
    public static boolean isLoggedIn() {return currentUser != null;}

    public static String username() {
        return currentUser.getUsername();
    }

    public static UserRole role() {
        return currentUser.getRole();
    }
}
