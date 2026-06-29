package fu.swt301.sms.service;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.PasswordUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Business-logic layer for employee management. Keeping this logic out of the
 * servlet (NFR-03) lets it be unit tested with JUnit 5 + Mockito by mocking the
 * {@link StaffDAO}.
 */
public class EmployeeService {

    private final StaffDAO staffDAO;
    private final EmployeeValidator validator;

    public EmployeeService() {
        this(new StaffDAO(), new EmployeeValidator());
    }

    public EmployeeService(StaffDAO staffDAO, EmployeeValidator validator) {
        this.staffDAO = staffDAO;
        this.validator = validator;
    }

    /**
     * Validates and creates a new staff member (FR-07). The plain-text password
     * is hashed with BCrypt before being persisted.
     *
     * @param staff         the staff data collected from the form
     * @param plainPassword the plain-text password typed on the create form
     * @return a list of error messages; empty when the staff was created
     * @throws SQLException           if a database access error occurs
     * @throws ClassNotFoundException if the JDBC driver is not found
     */
    public List<String> createStaff(Staff staff, String plainPassword)
            throws SQLException, ClassNotFoundException {

        List<String> errors = validator.validateForCreate(staff, plainPassword);
        // Skip database round-trips when the input is already malformed.
        if (!errors.isEmpty()) {
            return errors;
        }

        if (staffDAO.isStaffCodeExists(staff.getStaffCode(), 0)) {
            errors.add("Staff code already exists. Please choose another one.");
        }
        if (staffDAO.isEmailExists(staff.getEmail(), 0)) {
            errors.add("Email already exists. Please choose another one.");
        }
        if (staffDAO.isPhoneNumberExists(staff.getPhoneNumber(), 0)) {
            errors.add("Phone number already exists. Please choose another one.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        staff.setPassword(PasswordUtils.hashPassword(plainPassword));
        staffDAO.createStaff(staff);
        return errors;
    }
}
