package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class AuthService {

    /** Number of consecutive failed attempts that triggers a lockout. */
    public static final int MAX_ATTEMPTS = 5;
    /** Lockout duration in milliseconds (5 minutes). */
    public static final long LOCK_TIME_MS = 5L * 60 * 1000;

    private final StaffDAO staffDAO;
    private final LongSupplier clock;

    // Per-email lockout state shared across requests (servlet is a singleton).
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockedUntil = new ConcurrentHashMap<>();

    public AuthService() {
        this(new StaffDAO(), System::currentTimeMillis);
    }

    public AuthService(StaffDAO staffDAO, LongSupplier clock) {
        this.staffDAO = staffDAO;
        this.clock = clock;
    }

    /**
     * Authenticates a login attempt and applies the lockout policy.
     *
     * @param email    the submitted email
     * @param password the submitted plain-text password
     * @return a {@link LoginResult} describing success (with the user) or the
     *         failure message to display
     */
    public LoginResult login(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return LoginResult.failure("Invalid email or password.");
        }

        email = email.trim();
        password = password.trim();

        if (isAccountLocked(email)) {
            long remain = (lockedUntil.get(email) - now()) / 1000;
            return LoginResult.failure(
                    "Your account is locked. Please try again after " + remain + " seconds.");
        }

        Staff staff = staffDAO.checkLogin(email, password);
        if (staff != null) {
            resetLoginAttempt(email);
            staff.setPassword(null); // never keep the hash in the session
            return LoginResult.success(staff);
        }

        return LoginResult.failure(handleFailedLogin(email));
    }

    /**
     * @return {@code true} while the account is within its lockout window;
     *         an expired lock is cleared and reported as unlocked
     */
    public boolean isAccountLocked(String email) {
        Long lockTime = lockedUntil.get(email);
        if (lockTime == null) {
            return false;
        }
        if (now() >= lockTime) {
            resetLoginAttempt(email);
            return false;
        }
        return true;
    }

    private String handleFailedLogin(String email) {
        increaseFailedAttempt(email);

        if (isAccountLocked(email)) {
            return "Your account has been locked for 5 minutes.";
        }

        int count = failedAttempts.getOrDefault(email, 0);
        return "Invalid email or password. Attempt " + count + "/" + MAX_ATTEMPTS;
    }

    private void increaseFailedAttempt(String email) {
        int count = failedAttempts.getOrDefault(email, 0) + 1;
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(email, now() + LOCK_TIME_MS);
            failedAttempts.remove(email);
            staffDAO.updateActiveStatus(email, false);
        } else {
            failedAttempts.put(email, count);
        }
    }

    private void resetLoginAttempt(String email) {
        failedAttempts.remove(email);
        lockedUntil.remove(email);
    }

    private long now() {
        return clock.getAsLong();
    }
}
