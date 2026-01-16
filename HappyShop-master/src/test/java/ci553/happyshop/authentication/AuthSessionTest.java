package ci553.happyshop.authentication;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthSessionTest {

    @AfterEach
    void clear() {
        AuthSession.clear();
    }

    @Test
    void session_set_get_clear() {
        assertFalse(AuthSession.isLoggedIn());

        UserAccount u = new UserAccount("billy", UserRole.CUSTOMER);
        AuthSession.set(u);

        assertTrue(AuthSession.isLoggedIn());
        assertEquals("billy", AuthSession.username());
        assertEquals(UserRole.CUSTOMER, AuthSession.role());

        AuthSession.clear();
        assertFalse(AuthSession.isLoggedIn());
        assertNull(AuthSession.get());
    }
}
