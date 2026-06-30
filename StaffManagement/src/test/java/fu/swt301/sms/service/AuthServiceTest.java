package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService} (FR-01 login, FR-02 messages, FR-03 lockout).
 * <p>
 * {@link StaffDAO} is mocked and time is driven by a mutable clock, so the
 * 5-minute lockout window is tested deterministically without {@code Thread.sleep}.
 */
class AuthServiceTest {

    private StaffDAO dao;
    private long[] now;          // mutable clock backing store (millis)
    private AuthService auth;

    private static final String EMAIL = "admin@example.com";
    private static final String PASS = "admin123";

    @BeforeEach
    void setUp() {
        dao = mock(StaffDAO.class);
        now = new long[]{1_000_000L};
        LongSupplier clock = () -> now[0];
        auth = new AuthService(dao, clock);
    }

    private Staff user() {
        Staff s = new Staff();
        s.setEmail(EMAIL);
        s.setPassword("$2a$hashed");
        return s;
    }

    @Test
    void login_validCredentials_succeedsAndClearsPassword() {
        Staff s = user();
        when(dao.checkLogin(EMAIL, PASS)).thenReturn(s);

        LoginResult r = auth.login(EMAIL, PASS);

        assertTrue(r.isSuccess());
        assertSame(s, r.getStaff());
        assertNull(r.getStaff().getPassword(), "password must be cleared before session storage");
    }

    @Test
    void login_trimsEmailAndPassword() {
        when(dao.checkLogin(EMAIL, PASS)).thenReturn(user());

        LoginResult r = auth.login("  " + EMAIL + " ", "  " + PASS + " ");

        assertTrue(r.isSuccess());
        verify(dao).checkLogin(EMAIL, PASS);
    }

    @Test
    void login_blankInput_failsWithoutDb() {
        LoginResult r = auth.login("", "  ");
        assertFalse(r.isSuccess());
        org.junit.jupiter.api.Assertions.assertEquals("Invalid email or password.", r.getErrorMessage());
        verify(dao, never()).checkLogin(anyString(), anyString());
    }

    @Test
    void login_nullInput_failsWithoutDb() {
        LoginResult r = auth.login(null, null);
        assertFalse(r.isSuccess());
        verify(dao, never()).checkLogin(anyString(), anyString());
    }

    @Test
    void login_wrongPassword_firstAttempt_showsCount() {
        when(dao.checkLogin(EMAIL, "wrong")).thenReturn(null);

        LoginResult r = auth.login(EMAIL, "wrong");

        assertFalse(r.isSuccess());
        assertTrue(r.getErrorMessage().contains("Attempt 1/5"), r.getErrorMessage());
    }

    @Test
    void login_fifthWrongAttempt_locksAccount() {
        when(dao.checkLogin(anyString(), anyString())).thenReturn(null);

        LoginResult r = null;
        for (int i = 0; i < 5; i++) {
            r = auth.login(EMAIL, "wrong");
        }

        assertFalse(r.isSuccess());
        assertTrue(r.getErrorMessage().contains("locked for 5 minutes"), r.getErrorMessage());
        assertTrue(auth.isAccountLocked(EMAIL));
    }

    @Test
    void login_whenLocked_doesNotHitDbAndShowsRemaining() {
        when(dao.checkLogin(anyString(), anyString())).thenReturn(null);
        for (int i = 0; i < 5; i++) {
            auth.login(EMAIL, "wrong");
        }
        // 5 DAO calls so far; the 6th attempt is short-circuited by the lock.
        LoginResult r = auth.login(EMAIL, "wrong");

        assertFalse(r.isSuccess());
        assertTrue(r.getErrorMessage().contains("try again after"), r.getErrorMessage());
        verify(dao, times(5)).checkLogin(anyString(), anyString());
    }

    @Test
    void login_afterLockExpires_allowsLoginAgain() {
        when(dao.checkLogin(EMAIL, "wrong")).thenReturn(null);
        for (int i = 0; i < 5; i++) {
            auth.login(EMAIL, "wrong");
        }
        assertTrue(auth.isAccountLocked(EMAIL));

        // Advance the clock past the lockout window.
        now[0] += AuthService.LOCK_TIME_MS + 1;

        assertFalse(auth.isAccountLocked(EMAIL));
        when(dao.checkLogin(EMAIL, PASS)).thenReturn(user());
        assertTrue(auth.login(EMAIL, PASS).isSuccess());
    }

    @Test
    void isAccountLocked_unknownEmail_isFalse() {
        assertFalse(auth.isAccountLocked("nobody@example.com"));
    }
}
