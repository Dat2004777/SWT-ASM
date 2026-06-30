package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

/**
 * Immutable outcome of an authentication attempt (FR-01..FR-03).
 * <p>
 * Returned by {@link AuthService#login(String, String)} so the servlet only has
 * to decide where to navigate, keeping all auth/lockout logic in the service
 * layer (NFR-03) where it can be unit tested without a servlet container.
 */
public class LoginResult {

    private final boolean success;
    private final Staff staff;
    private final String errorMessage;

    private LoginResult(boolean success, Staff staff, String errorMessage) {
        this.success = success;
        this.staff = staff;
        this.errorMessage = errorMessage;
    }

    /** @param staff the authenticated user (password already cleared) */
    public static LoginResult success(Staff staff) {
        return new LoginResult(true, staff, null);
    }

    public static LoginResult failure(String errorMessage) {
        return new LoginResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    /** @return the authenticated staff on success, or {@code null} on failure */
    public Staff getStaff() {
        return staff;
    }

    /** @return the error message to show on failure, or {@code null} on success */
    public String getErrorMessage() {
        return errorMessage;
    }
}
