package ci553.happyshop.authentication;

import java.util.Map;

public final class AuthService {
    //test accounts
    private static final String STAFF_USER = "staff1";
    private static final String STAFF_PASS = "staff1";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";

    private AuthService() {}

    public static UserAccount authenticate(String username, String password) {
        if (username == null || password == null) return null;

        String u = username.trim();
        String p = password.trim();

        if (u.isEmpty() || p.isEmpty()) return null;
        if (u.equals(ADMIN_USER) && p.equals(ADMIN_PASS)) { //roles for testing
            return new UserAccount(u, UserRole.ADMIN);
        }
        if (u.equals(STAFF_USER) && p.equals(STAFF_PASS)) {
            return new UserAccount(u, UserRole.STAFF);
        }
        Map<String, String> customers = UserStore.loadCustomers(); //customers are stores in users.csv
        String stored = customers.get(u);
        if (stored != null && stored.equals(p)) {
            return new UserAccount(u, UserRole.CUSTOMER);
        }

        return null;
    }
    //returns null if successful
    public static String registerCustomer(String username, String password, String confirmPassword) {
        if (username == null || password == null || confirmPassword == null) return "Missing fields";

        String u = username.trim();
        String p = password.trim();
        String c = confirmPassword.trim();

        //errors
        if (u.isEmpty()) return "username is required";
        if (u.contains(" ") || u.contains(",")) return "Username cannot contain spaces or commas";
        if (u.length() < 3) return "username must be at least 3 characters";
        if (p.length() < 4) return "password must be at least 4 characters";
        if (!p.equals(c)) return "passwords do not match";
        if (u.equals(ADMIN_USER) || u.equals(STAFF_USER)) return "That username is reserved";
        Map<String, String> customers = UserStore.loadCustomers();
        if (customers.containsKey(u)) return "Username already exists";

        customers.put(u, p);
        UserStore.saveCustomers(customers);

        return null;
    }
}

