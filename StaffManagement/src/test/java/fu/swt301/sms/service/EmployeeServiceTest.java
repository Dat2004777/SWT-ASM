package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EmployeeService} (NFR-05 / FR-07 + FR-10).
 * <p>
 * {@link StaffDAO} is mocked with Mockito so no database is required; the real
 * {@link EmployeeValidator} is used so the validate-then-persist flow is exercised
 * end to end.
 */
class EmployeeServiceTest {

    private StaffDAO staffDAO;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        staffDAO = mock(StaffDAO.class);
        service = new EmployeeService(staffDAO, new EmployeeValidator());
    }

    private static Staff validStaff() {
        Staff s = new Staff();
        s.setStaffCode("EMP001");
        s.setFullName("Nguyen Van A");
        s.setEmail("a@example.com");
        s.setPhoneNumber("0901234567");
        s.setDepartment("IT");
        s.setPosition("Developer");
        s.setSalary(new BigDecimal("15000000"));
        s.setDateOfBirth(LocalDate.now().minusYears(30));
        s.setHireDate(LocalDate.now().minusDays(1));
        return s;
    }

    // ---------- createStaff ----------

    @Test
    void createStaff_invalidInput_returnsErrorsAndSkipsDb() throws Exception {
        Staff s = validStaff();
        s.setEmail("bad-email");

        List<String> errors = service.createStaff(s, "123456");

        assertTrue(errors.contains("Email format is invalid."));
        // No DB round-trip when validation already failed.
        verify(staffDAO, never()).isEmailExists(anyString(), anyInt());
        verify(staffDAO, never()).createStaff(any());
    }

    @Test
    void createStaff_duplicateStaffCode_returnsError() throws Exception {
        when(staffDAO.isStaffCodeExists(eq("EMP001"), eq(0))).thenReturn(true);

        List<String> errors = service.createStaff(validStaff(), "123456");

        assertTrue(errors.contains("Staff code already exists. Please choose another one."));
        verify(staffDAO, never()).createStaff(any());
    }

    @Test
    void createStaff_duplicateEmail_returnsError() throws Exception {
        when(staffDAO.isEmailExists(eq("a@example.com"), eq(0))).thenReturn(true);

        List<String> errors = service.createStaff(validStaff(), "123456");

        assertTrue(errors.contains("Email already exists. Please choose another one."));
        verify(staffDAO, never()).createStaff(any());
    }

    @Test
    void createStaff_duplicatePhone_returnsError() throws Exception {
        when(staffDAO.isPhoneNumberExists(eq("0901234567"), eq(0))).thenReturn(true);

        List<String> errors = service.createStaff(validStaff(), "123456");

        assertTrue(errors.contains("Phone number already exists. Please choose another one."));
        verify(staffDAO, never()).createStaff(any());
    }

    @Test
    void createStaff_validUniqueInput_hashesPasswordAndPersists() throws Exception {
        // All uniqueness checks default to false on the mock.
        Staff s = validStaff();

        List<String> errors = service.createStaff(s, "123456");

        assertTrue(errors.isEmpty(), "expected success but got: " + errors);
        // Password must be BCrypt-hashed, never stored in plain text.
        assertTrue(s.getPassword().startsWith("$2"), "password was not BCrypt-hashed");
        org.junit.jupiter.api.Assertions.assertNotEquals("123456", s.getPassword());
        verify(staffDAO, times(1)).createStaff(s);
    }

    // ---------- getStaffById ----------

    @Test
    void getStaffById_zeroId_returnsNullWithoutDb() {
        assertNull(service.getStaffById(0));
        verify(staffDAO, never()).getStaffById(anyInt());
    }

    @Test
    void getStaffById_negativeId_returnsNullWithoutDb() {
        assertNull(service.getStaffById(-5));
        verify(staffDAO, never()).getStaffById(anyInt());
    }

    @Test
    void getStaffById_found_returnsStaff() {
        Staff expected = validStaff();
        expected.setStaffID(5);
        when(staffDAO.getStaffById(5)).thenReturn(expected);

        assertSame(expected, service.getStaffById(5));
    }

    @Test
    void getStaffById_notFound_returnsNull() {
        when(staffDAO.getStaffById(999)).thenReturn(null);
        assertNull(service.getStaffById(999));
    }

    @Test
    void updateStaff_validInput_updatesSuccessfully() throws Exception {
        Staff s = validStaff();
        s.setStaffID(1); // Cần có ID để update
        when(staffDAO.isStaffCodeExists(eq(s.getStaffCode()), eq(s.getStaffID()))).thenReturn(false);
        when(staffDAO.isEmailExists(eq(s.getEmail()), eq(s.getStaffID()))).thenReturn(false);
        when(staffDAO.isPhoneNumberExists(eq(s.getPhoneNumber()), eq(s.getStaffID()))).thenReturn(false);
        List<String> errors = service.updateStaff(s);
        assertTrue(errors.isEmpty(), "Expected no validation errors, but got: " + errors);
        verify(staffDAO, times(1)).updateStaff(s);
    }

    @Test
    void updateStaff_duplicateStaffCode_returnsError() throws Exception {
        Staff s = validStaff();
        s.setStaffID(1);
        when(staffDAO.isStaffCodeExists(eq(s.getStaffCode()), eq(s.getStaffID()))).thenReturn(true);
        List<String> errors = service.updateStaff(s);
        assertTrue(errors.contains("Staff code already exists. Please choose another one."));
        verify(staffDAO, never()).updateStaff(s);
    }

    @Test
    void updateStaff_duplicateEmail_returnsError() throws Exception {
        Staff s = validStaff();
        s.setStaffID(1);
        when(staffDAO.isEmailExists(eq(s.getEmail()), eq(s.getStaffID()))).thenReturn(true);
        List<String> errors = service.updateStaff(s);
        assertTrue(errors.contains("Email already exists. Please choose another one."));
        verify(staffDAO, never()).updateStaff(s);
    }

    @Test
    void updateStaff_duplicatePhone_returnsError() throws Exception {
        Staff s = validStaff();
        s.setStaffID(1);
        when(staffDAO.isPhoneNumberExists(eq(s.getPhoneNumber()), eq(s.getStaffID()))).thenReturn(true);
        List<String> errors = service.updateStaff(s);
        assertTrue(errors.contains("Phone number already exists. Please choose another one."));
        verify(staffDAO, never()).updateStaff(s);
    }

    @Test
    void deleteStaff_hardDelete_callsDaoDelete() {
        int targetStaffId = 10;
        service.deleteStaff(targetStaffId);
        verify(staffDAO, times(1)).deleteStaff(targetStaffId);
        verify(staffDAO, never()).updateStaff(any());
    }

    @Test
    void deleteStaff_zeroOrNegativeId_doesNotCallDao() {
        service.deleteStaff(0);
        service.deleteStaff(-5);
        verify(staffDAO, never()).deleteStaff(anyInt());
    }
}
