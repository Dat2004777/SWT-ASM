package fu.swt301.sms.servlet;

import fu.swt301.sms.dao.RoleDAO;
import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.EmployeeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controller for all CRUD operations on Staff. The create flow (FR-07) delegates
 * its business logic (validation, uniqueness, password hashing) to
 * {@link EmployeeService}, keeping this servlet thin (NFR-03).
 */
@WebServlet("/staff-crud")
public class StaffCrudServlet extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final StaffDAO staffDAO = new StaffDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    /**
     * Handles POST requests used to create or update a staff member.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Staff staff = buildStaffFromRequest(request);
        String password = request.getParameter("password");

        if ("create".equals(action)) {
            List<String> errors;
            try {
                errors = employeeService.createStaff(staff, password);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                errors = Collections.singletonList("Database error during creation.");
            }
            if (!errors.isEmpty()) {
                forwardBackToForm(request, response, staff, String.join(" ", errors));
                return;
            }
            response.sendRedirect("staff-list");
            return;
        }

        // --- update branch (FR-08) ---
        String errorMessage = null;
        try {
            if (staffDAO.isEmailExists(staff.getEmail(), staff.getStaffID())) {
                errorMessage = "Email already exists. Please choose another one.";
            } else if (staffDAO.isPhoneNumberExists(staff.getPhoneNumber(), staff.getStaffID())) {
                errorMessage = "Phone number already exists. Please choose another one.";
            } else if (staffDAO.isStaffCodeExists(staff.getStaffCode(), staff.getStaffID())) {
                errorMessage = "Staff code already exists. Please choose another one.";
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            errorMessage = "Database error during validation.";
        }

        if (errorMessage != null) {
            forwardBackToForm(request, response, staff, errorMessage);
            return;
        }

        staffDAO.updateStaff(staff);
        response.sendRedirect("staff-list");
    }

    /**
     * Handles GET requests used to display forms or perform a deletion.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            int staffId = Integer.parseInt(request.getParameter("id"));
            staffDAO.deleteStaff(staffId);
            response.sendRedirect("staff-list");
            return;
        }

        request.setAttribute("roleList", roleDAO.getAllRoles());
        if ("edit".equals(action)) {
            int staffId = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("staff", staffDAO.getStaffById(staffId));
        }
        request.getRequestDispatcher("staff-form.jsp").forward(request, response);
    }

    /** Populates a Staff object from the submitted form, tolerating missing/invalid input. */
    private Staff buildStaffFromRequest(HttpServletRequest request) {
        Staff staff = new Staff();
        staff.setStaffID(parseInt(request.getParameter("staffID"), 0));
        staff.setStaffCode(trim(request.getParameter("staffCode")));
        staff.setFullName(trim(request.getParameter("fullName")));
        staff.setDateOfBirth(parseDate(request.getParameter("dateOfBirth")));
        staff.setGender(Boolean.parseBoolean(request.getParameter("gender")));
        staff.setPhoneNumber(trim(request.getParameter("phoneNumber")));
        staff.setEmail(trim(request.getParameter("email")));
        staff.setDepartment(trim(request.getParameter("department")));
        staff.setPosition(trim(request.getParameter("position")));
        staff.setSalary(parseBigDecimal(request.getParameter("salary")));
        staff.setHireDate(parseDate(request.getParameter("hireDate")));
        staff.setIsActive(Boolean.parseBoolean(request.getParameter("isActive")));

        Role role = new Role();
        role.setRoleID(parseInt(request.getParameter("roleID"), 0));
        staff.setRole(role);
        return staff;
    }

    /** Re-displays the form with an error message and the user's preserved input. */
    private void forwardBackToForm(HttpServletRequest request, HttpServletResponse response,
                                   Staff staff, String errorMessage) throws ServletException, IOException {
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("staff", staff);
        request.setAttribute("roleList", roleDAO.getAllRoles());
        request.getRequestDispatcher("staff-form.jsp").forward(request, response);
    }

    private static String trim(String value) {
        return value != null ? value.trim() : null;
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return (value != null && !value.isEmpty()) ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
