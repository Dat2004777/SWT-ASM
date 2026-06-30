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

    /**
     * Retrieves a single staff member's full details (FR-10).
     * <p>
     * A non-positive id can never match a real record (StaffID is an IDENTITY
     * starting at 1), so it short-circuits to {@code null} without touching the
     * database. This guard keeps the method unit-testable without a DB and makes
     * the "not found" path explicit for the servlet.
     *
     * @param staffId the id of the staff member to view
     * @return the populated {@link Staff}, or {@code null} when the id is invalid
     *         or no matching record exists
     */
    public Staff getStaffById(int staffId) {
        if (staffId <= 0) {
            return null;
        }
        return staffDAO.getStaffById(staffId);
    }
    public List<String> updateStaff(Staff staff) {
        List<String> errors = new java.util.ArrayList<>();

        try {
            // 1. Kiểm tra tính hợp lệ của dữ liệu (Validation)
            // Tương tự như lúc Create, nhưng ta truyền vào staff.getStaffID() để bỏ qua chính nó khi check trùng lặp
            if (staffDAO.isStaffCodeExists(staff.getStaffCode(), staff.getStaffID())) {
                errors.add("Staff code already exists. Please choose another one.");
            }
            if (staffDAO.isEmailExists(staff.getEmail(), staff.getStaffID())) {
                errors.add("Email already exists. Please choose another one.");
            }
            if (staffDAO.isPhoneNumberExists(staff.getPhoneNumber(), staff.getStaffID())) {
                errors.add("Phone number already exists. Please choose another one.");
            }

            // (Bạn có thể thêm các hàm validate định dạng email, sđt bằng EmployeeValidator ở đây nếu cần)

            // 2. Nếu không có lỗi nào, tiến hành gọi DAO để cập nhật
            if (errors.isEmpty()) {
                staffDAO.updateStaff(staff);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errors.add("Lỗi hệ thống khi cập nhật nhân viên: " + e.getMessage());
        }

        return errors;
    }
    public void deleteStaff(int staffId) {
        // Vì yêu cầu của bạn là xóa cứng, ta chỉ cần gọi trực tiếp lệnh deleteStaff từ DAO
        staffDAO.deleteStaff(staffId);
    }
}
