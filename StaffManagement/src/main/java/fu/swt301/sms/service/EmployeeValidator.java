package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pure (database-free) input validation for the Staff/Employee form (FR-07).
 * <p>
 * Every field has its own {@code validateXxx} method that returns {@code null}
 * when the value is valid, or a human readable error message otherwise. Methods
 * that depend on the current date are overloaded with a {@code today} parameter
 * so unit tests can apply Boundary Value Analysis deterministically (e.g. for
 * {@code salary}, {@code dateOfBirth} and {@code hireDate}).
 */
public class EmployeeValidator {

    /** Inclusive lower bound for a valid monthly salary (VND). */
    public static final BigDecimal MIN_SALARY = new BigDecimal("1000000");
    /** Inclusive upper bound for a valid monthly salary (VND). */
    public static final BigDecimal MAX_SALARY = new BigDecimal("1000000000");
    /** Inclusive lower bound for the employee's age (years). */
    public static final int MIN_AGE = 18;
    /** Inclusive upper bound for the employee's age (years). */
    public static final int MAX_AGE = 65;
    /** Inclusive password length bounds (only required when creating). */
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;

    private static final Pattern STAFF_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9]{3,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    public String validateStaffCode(String staffCode) {
        if (isBlank(staffCode)) {
            return "Staff code is required.";
        }
        if (!STAFF_CODE_PATTERN.matcher(staffCode.trim()).matches()) {
            return "Staff code must be 3-20 letters or digits.";
        }
        return null;
    }

    public String validateFullName(String fullName) {
        if (isBlank(fullName)) {
            return "Full name is required.";
        }
        if (fullName.trim().length() > 100) {
            return "Full name must not exceed 100 characters.";
        }
        return null;
    }

    public String validateEmail(String email) {
        if (isBlank(email)) {
            return "Email is required.";
        }
        String value = email.trim();
        if (value.length() > 100) {
            return "Email must not exceed 100 characters.";
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            return "Email format is invalid.";
        }
        return null;
    }

    public String validatePhoneNumber(String phoneNumber) {
        if (isBlank(phoneNumber)) {
            return "Phone number is required.";
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            return "Phone number must be 10 digits and start with 0.";
        }
        return null;
    }

    public String validateDepartment(String department) {
        if (isBlank(department)) {
            return "Department is required.";
        }
        if (department.trim().length() > 100) {
            return "Department must not exceed 100 characters.";
        }
        return null;
    }

    public String validatePosition(String position) {
        if (isBlank(position)) {
            return "Position is required.";
        }
        if (position.trim().length() > 100) {
            return "Position must not exceed 100 characters.";
        }
        return null;
    }

    public String validateSalary(BigDecimal salary) {
        if (salary == null) {
            return "Salary is required.";
        }
        if (salary.compareTo(MIN_SALARY) < 0 || salary.compareTo(MAX_SALARY) > 0) {
            return "Salary must be between " + MIN_SALARY.toPlainString()
                    + " and " + MAX_SALARY.toPlainString() + ".";
        }
        return null;
    }

    public String validateDateOfBirth(LocalDate dateOfBirth) {
        return validateDateOfBirth(dateOfBirth, LocalDate.now());
    }

    public String validateDateOfBirth(LocalDate dateOfBirth, LocalDate today) {
        if (dateOfBirth == null) {
            return "Date of birth is required.";
        }
        if (dateOfBirth.isAfter(today)) {
            return "Date of birth cannot be in the future.";
        }
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < MIN_AGE) {
            return "Employee must be at least " + MIN_AGE + " years old.";
        }
        if (age > MAX_AGE) {
            return "Employee must be at most " + MAX_AGE + " years old.";
        }
        return null;
    }

    public String validateHireDate(LocalDate hireDate) {
        return validateHireDate(hireDate, LocalDate.now());
    }

    public String validateHireDate(LocalDate hireDate, LocalDate today) {
        if (hireDate == null) {
            return "Hire date is required.";
        }
        if (hireDate.isAfter(today)) {
            return "Hire date cannot be in the future.";
        }
        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return "Password must be between " + MIN_PASSWORD_LENGTH
                    + " and " + MAX_PASSWORD_LENGTH + " characters.";
        }
        return null;
    }

    /**
     * Aggregates all field validations for creating a new staff member.
     *
     * @param staff         the staff data collected from the form
     * @param plainPassword the plain-text password typed on the create form
     * @param today         reference date (injected for deterministic tests)
     * @return a list of error messages; empty when the staff is valid
     */
    public List<String> validateForCreate(Staff staff, String plainPassword, LocalDate today) {
        List<String> errors = new ArrayList<>();
        if (staff == null) {
            errors.add("Staff data is required.");
            return errors;
        }
        addIfPresent(errors, validateStaffCode(staff.getStaffCode()));
        addIfPresent(errors, validateFullName(staff.getFullName()));
        addIfPresent(errors, validateEmail(staff.getEmail()));
        addIfPresent(errors, validatePhoneNumber(staff.getPhoneNumber()));
        addIfPresent(errors, validateDepartment(staff.getDepartment()));
        addIfPresent(errors, validatePosition(staff.getPosition()));
        addIfPresent(errors, validateSalary(staff.getSalary()));
        addIfPresent(errors, validateDateOfBirth(staff.getDateOfBirth(), today));
        addIfPresent(errors, validateHireDate(staff.getHireDate(), today));
        addIfPresent(errors, validatePassword(plainPassword));
        return errors;
    }

    public List<String> validateForCreate(Staff staff, String plainPassword) {
        return validateForCreate(staff, plainPassword, LocalDate.now());
    }

    private static void addIfPresent(List<String> errors, String message) {
        if (message != null) {
            errors.add(message);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
