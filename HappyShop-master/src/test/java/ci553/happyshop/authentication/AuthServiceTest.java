package ci553.happyshop.authentication;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.csv");

    @BeforeEach
    void setup() throws Exception {
        Files.createDirectories(DATA_DIR);
        // reset file
        Files.write(USERS_FILE,
                List.of("# username,password", "billy,pass1234"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @AfterEach
    void teardown() throws Exception {
        Files.deleteIfExists(USERS_FILE);
        // only delete /data if empty
        try (var s = Files.list(DATA_DIR)) {
            if (s.findAny().isEmpty()) Files.deleteIfExists(DATA_DIR);
        }
    }

    @Test
    void authenticate_admin_success() {
        UserAccount u = AuthService.authenticate("admin", "admin");
        assertNotNull(u);
        assertEquals("admin", u.getUsername());
        assertEquals(UserRole.ADMIN, u.getRole());
    }

    @Test
    void authenticate_staff_success() {
        UserAccount u = AuthService.authenticate("staff1", "staff1");
        assertNotNull(u);
        assertEquals(UserRole.STAFF, u.getRole());
    }

    @Test
    void authenticate_customer_success_fromCsv() {
        UserAccount u = AuthService.authenticate("billy", "pass1234");
        assertNotNull(u);
        assertEquals("billy", u.getUsername());
        assertEquals(UserRole.CUSTOMER, u.getRole());
    }

    @Test
    void authenticate_failure_wrongPassword() {
        UserAccount u = AuthService.authenticate("billy", "wrong");
        assertNull(u);
    }

    @Test
    void registerCustomer_rejectsShortUsername() {
        String err = AuthService.registerCustomer("ab", "1234", "1234");
        assertNotNull(err);
    }

    @Test
    void registerCustomer_rejectsMismatchPasswords() {
        String err = AuthService.registerCustomer("katherine", "1234", "9999");
        assertNotNull(err);
    }

    @Test
    void registerCustomer_rejectsDuplicateUsername() {
        String err = AuthService.registerCustomer("billy", "1234", "1234");
        assertNotNull(err);
    }

    @Test
    void registerCustomer_success_persistsToCsv() throws Exception {
        String err = AuthService.registerCustomer("katherine", "1234", "1234");
        assertNull(err);

        // authenticate should now succeed
        UserAccount u = AuthService.authenticate("katherine", "1234");
        assertNotNull(u);
        assertEquals(UserRole.CUSTOMER, u.getRole());

        // file should contain katherine
        String file = Files.readString(USERS_FILE, StandardCharsets.UTF_8);
        assertTrue(file.contains("katherine,1234"));
    }
}
