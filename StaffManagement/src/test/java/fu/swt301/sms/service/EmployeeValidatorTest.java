package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EmployeeValidator} (NFR-05 / FR-07).
 * <p>
 * Uses Equivalence Partitioning + Boundary Value Analysis. The date-dependent
 * rules are tested against a fixed {@code today} so they never flake.
 */
class EmployeeValidatorTest {

    private final EmployeeValidator validator = new EmployeeValidator();
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 30);

    /** Builds a Staff that passes every field validation. */
    private static Staff validStaff() {
        Staff s = new Staff();
        s.setStaffCode("EMP001");
        s.setFullName("Nguyen Van A");
        s.setEmail("a@example.com");
        s.setPhoneNumber("0901234567");
        s.setDepartment("IT");
        s.setPosition("Developer");
        s.setSalary(new BigDecimal("15000000"));
        s.setDateOfBirth(LocalDate.of(2000, 1, 1)); // age 26 at TODAY
        s.setHireDate(LocalDate.of(2025, 1, 1));
        return s;
    }

    // ---------- staff code ----------
    @Test
    void staffCode_null_isRequired() {
        assertEquals("Staff code is required.", validator.validateStaffCode(null));
    }

    @Test
    void staffCode_blank_isRequired() {
        assertEquals("Staff code is required.", validator.validateStaffCode("   "));
    }

    @Test
    void staffCode_tooShort_invalid() {
        assertNotNull(validator.validateStaffCode("ab"));
    }

    @Test
    void staffCode_tooLong_invalid() {
        assertNotNull(validator.validateStaffCode("A12345678901234567890")); // 21 chars
    }

    @Test
    void staffCode_specialChar_invalid() {
        assertNotNull(validator.validateStaffCode("EMP_01"));
    }

    @Test
    void staffCode_valid_returnsNull() {
        assertNull(validator.validateStaffCode("EMP001"));
    }

    // ---------- full name ----------
    @Test
    void fullName_blank_isRequired() {
        assertEquals("Full name is required.", validator.validateFullName(" "));
    }

    @Test
    void fullName_tooLong_invalid() {
        assertNotNull(validator.validateFullName("x".repeat(101)));
    }

    @Test
    void fullName_valid_returnsNull() {
        assertNull(validator.validateFullName("Nguyen Van A"));
    }

    // ---------- email ----------
    @Test
    void email_blank_isRequired() {
        assertEquals("Email is required.", validator.validateEmail(""));
    }

    @Test
    void email_tooLong_invalid() {
        assertNotNull(validator.validateEmail("a".repeat(95) + "@x.com"));
    }

    @Test
    void email_badFormat_invalid() {
        assertNotNull(validator.validateEmail("not-an-email"));
    }

    @Test
    void email_valid_returnsNull() {
        assertNull(validator.validateEmail("a@example.com"));
    }

    // ---------- phone ----------
    @Test
    void phone_blank_isRequired() {
        assertEquals("Phone number is required.", validator.validatePhoneNumber(null));
    }

    @Test
    void phone_notStartingWithZero_invalid() {
        assertNotNull(validator.validatePhoneNumber("1901234567"));
    }

    @Test
    void phone_wrongLength_invalid() {
        assertNotNull(validator.validatePhoneNumber("090123456")); // 9 digits
    }

    @Test
    void phone_valid_returnsNull() {
        assertNull(validator.validatePhoneNumber("0901234567"));
    }

    // ---------- department / position ----------
    @Test
    void department_blank_isRequired() {
        assertEquals("Department is required.", validator.validateDepartment(""));
    }

    @Test
    void department_tooLong_invalid() {
        assertNotNull(validator.validateDepartment("d".repeat(101)));
    }

    @Test
    void department_valid_returnsNull() {
        assertNull(validator.validateDepartment("IT"));
    }

    @Test
    void position_blank_isRequired() {
        assertEquals("Position is required.", validator.validatePosition(null));
    }

    @Test
    void position_tooLong_invalid() {
        assertNotNull(validator.validatePosition("p".repeat(101)));
    }

    @Test
    void position_valid_returnsNull() {
        assertNull(validator.validatePosition("Developer"));
    }

    // ---------- salary (BVA) ----------
    @Test
    void salary_null_isRequired() {
        assertEquals("Salary is required.", validator.validateSalary(null));
    }

    @Test
    void salary_belowMin_invalid() {
        assertNotNull(validator.validateSalary(EmployeeValidator.MIN_SALARY.subtract(BigDecimal.ONE)));
    }

    @Test
    void salary_aboveMax_invalid() {
        assertNotNull(validator.validateSalary(EmployeeValidator.MAX_SALARY.add(BigDecimal.ONE)));
    }

    @Test
    void salary_atMinBoundary_valid() {
        assertNull(validator.validateSalary(EmployeeValidator.MIN_SALARY));
    }

    @Test
    void salary_atMaxBoundary_valid() {
        assertNull(validator.validateSalary(EmployeeValidator.MAX_SALARY));
    }

    // ---------- date of birth (BVA against fixed today) ----------
    @Test
    void dob_null_isRequired() {
        assertEquals("Date of birth is required.", validator.validateDateOfBirth(null, TODAY));
    }

    @Test
    void dob_future_invalid() {
        assertNotNull(validator.validateDateOfBirth(TODAY.plusDays(1), TODAY));
    }

    @Test
    void dob_tooYoung_invalid() {
        assertNotNull(validator.validateDateOfBirth(TODAY.minusYears(17), TODAY));
    }

    @Test
    void dob_tooOld_invalid() {
        assertNotNull(validator.validateDateOfBirth(TODAY.minusYears(66), TODAY));
    }

    @Test
    void dob_atMinAgeBoundary_valid() {
        assertNull(validator.validateDateOfBirth(TODAY.minusYears(18), TODAY));
    }

    @Test
    void dob_atMaxAgeBoundary_valid() {
        assertNull(validator.validateDateOfBirth(TODAY.minusYears(65), TODAY));
    }

    @Test
    void dob_noTodayOverload_runs() {
        // Exercises the LocalDate.now() overload; a clearly valid age must pass.
        assertNull(validator.validateDateOfBirth(LocalDate.now().minusYears(30)));
    }

    // ---------- hire date ----------
    @Test
    void hireDate_null_isRequired() {
        assertEquals("Hire date is required.", validator.validateHireDate(null, TODAY));
    }

    @Test
    void hireDate_future_invalid() {
        assertNotNull(validator.validateHireDate(TODAY.plusDays(1), TODAY));
    }

    @Test
    void hireDate_today_valid() {
        assertNull(validator.validateHireDate(TODAY, TODAY));
    }

    @Test
    void hireDate_noTodayOverload_runs() {
        assertNull(validator.validateHireDate(LocalDate.now().minusDays(1)));
    }

    // ---------- password (BVA) ----------
    @Test
    void password_null_isRequired() {
        assertEquals("Password is required.", validator.validatePassword(null));
    }

    @Test
    void password_empty_isRequired() {
        assertEquals("Password is required.", validator.validatePassword(""));
    }

    @Test
    void password_tooShort_invalid() {
        assertNotNull(validator.validatePassword("12345")); // 5
    }

    @Test
    void password_tooLong_invalid() {
        assertNotNull(validator.validatePassword("x".repeat(51)));
    }

    @Test
    void password_atMinBoundary_valid() {
        assertNull(validator.validatePassword("123456")); // 6
    }

    @Test
    void password_atMaxBoundary_valid() {
        assertNull(validator.validatePassword("x".repeat(50)));
    }

    // ---------- aggregator ----------
    @Test
    void validateForCreate_nullStaff_singleError() {
        List<String> errors = validator.validateForCreate(null, "123456", TODAY);
        assertEquals(List.of("Staff data is required."), errors);
    }

    @Test
    void validateForCreate_allValid_noErrors() {
        List<String> errors = validator.validateForCreate(validStaff(), "123456", TODAY);
        assertTrue(errors.isEmpty(), "expected no errors but got: " + errors);
    }

    @Test
    void validateForCreate_multipleBadFields_collectsAll() {
        Staff s = validStaff();
        s.setStaffCode("");      // 1 error
        s.setEmail("bad");       // 1 error
        s.setSalary(BigDecimal.ZERO); // 1 error
        List<String> errors = validator.validateForCreate(s, "123456", TODAY);
        assertEquals(3, errors.size(), "errors: " + errors);
    }

    @Test
    void validateForCreate_noTodayOverload_runs() {
        Staff s = validStaff();
        s.setDateOfBirth(LocalDate.now().minusYears(30));
        s.setHireDate(LocalDate.now().minusDays(1));
        assertFalse(validator.validateForCreate(s, "123456").contains("Date of birth is required."));
    }
}
