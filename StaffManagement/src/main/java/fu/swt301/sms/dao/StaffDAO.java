package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.entity.PageResult;
import fu.swt301.sms.utils.DBUtils;
import fu.swt301.sms.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the Staff entity. This class provides all the
 * necessary methods to interact with the 'Staff' table in the database. It
 * handles all CRUD (Create, Read, Update, Delete) operations as well as other
 * specific queries.
 */
public class StaffDAO {

    /**
     * A private helper method to map a row from the ResultSet to a Staff
     * object. This avoids code duplication in methods that retrieve staff data.
     * It performs a JOIN with the Role table to populate the nested Role
     * object.
     *
     * @param rs The ResultSet cursor, positioned at the row to be mapped.
     * @return A fully populated Staff object.
     * @throws SQLException if a database access error occurs.
     */
    private Staff extractStaffFromResultSet(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setStaffID(rs.getInt("StaffID"));
        staff.setStaffCode(rs.getString("StaffCode"));
        staff.setFullName(rs.getString("FullName"));
        Date dob = rs.getDate("DateOfBirth");
        staff.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        staff.setGender(rs.getBoolean("Gender"));
        staff.setPhoneNumber(rs.getString("PhoneNumber"));
        staff.setEmail(rs.getString("Email"));
        staff.setDepartment(rs.getString("Department"));
        staff.setPosition(rs.getString("Position"));
        staff.setSalary(rs.getBigDecimal("Salary"));
        Date hire = rs.getDate("HireDate");
        staff.setHireDate(hire != null ? hire.toLocalDate() : null);
        staff.setIsActive(rs.getBoolean("IsActive"));

        Role role = new Role();
        role.setRoleID(rs.getInt("Role_ID"));
        role.setRoleName(rs.getString("Role_Name"));
        staff.setRole(role);

        return staff;
    }

    /**
     * Checks if a given email already exists in the Staff table, excluding a
     * specific staff member. This is crucial for validation during updates to
     * prevent a user from taking another user's email.
     *
     * @param email The email to check for existence.
     * @param currentStaffId The ID of the staff member being updated. Use 0
     * when creating a new staff member.
     * @return true if the email exists for another staff member, false
     * otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    public boolean isEmailExists(String email, int currentStaffId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE Email = ? AND StaffID != ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if a given full name already exists in the Staff table, excluding
     * a specific staff member.
     *
     * @param fullName The full name to check.
     * @param currentStaffId The ID of the staff member being updated. Use 0 for
     * new staff.
     * @return true if the name exists for another staff member, false
     * otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    public boolean isFullNameExists(String fullName, int currentStaffId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE FullName = ? AND StaffID != ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setInt(2, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if a given phone number already exists in the Staff table,
     * excluding a specific staff member.
     *
     * @param phoneNumber The phone number to check.
     * @param currentStaffId The ID of the staff member being updated. Use 0 for
     * new staff.
     * @return true if the phone number exists for another staff member, false
     * otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    public boolean isPhoneNumberExists(String phoneNumber, int currentStaffId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE PhoneNumber = ? AND StaffID != ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);
            ps.setInt(2, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if a given staff code already exists, excluding a specific staff member.
     *
     * @param staffCode      The staff code to check.
     * @param currentStaffId The ID of the staff member being updated. Use 0 for new staff.
     * @return true if the staff code exists for another staff member, false otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws ClassNotFoundException if the database driver is not found.
     */
    public boolean isStaffCodeExists(String staffCode, int currentStaffId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM Staff WHERE StaffCode = ? AND StaffID != ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffCode);
            ps.setInt(2, currentStaffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Authenticates a user by checking their email and password against the
     * database.
     *
     * @param email The user's email.
     * @param password The user's plain text password.
     * @return A populated Staff object if authentication is successful, null
     * otherwise.
     */
//    public Staff checkLogin(String email, String password) {
//        String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.Email = ?";
//        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, email);
//            ps.setString(2, password);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return extractStaffFromResultSet(rs);
//                }
//            }
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    public Staff checkLogin(String email, String password) {
        String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.Email = ?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordInDB = rs.getString("Password");

                    if (PasswordUtils.verifyPassword(password, hashedPasswordInDB)) {
                        return extractStaffFromResultSet(rs);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    /**
     * Retrieves a list of staff members based on optional filter criteria.
     *
     * @param name A string to search for in the full name (case-insensitive).
     * Can be null or empty.
     * @param status The active status to filter by ("true" or "false"). Can be
     * null or empty.
     * @return A list of Staff objects matching the criteria.
     */
    public List<Staff> getStaffByFilter(String name, String status) {
        List<Staff> staffList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE 1=1");
        if (name != null && !name.isEmpty()) {
            sql.append(" AND s.FullName LIKE ?");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND s.IsActive = ?");
        }
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (name != null && !name.isEmpty()) {
                ps.setString(paramIndex++, "%" + name + "%");
            }
            if (status != null && !status.isEmpty()) {
                ps.setBoolean(paramIndex, Boolean.parseBoolean(status));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    staffList.add(extractStaffFromResultSet(rs));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }


    public PageResult<Staff> search(String name, Integer staffId, String status, int requestedPage, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            where.append(" AND LOWER(s.FullName) LIKE LOWER(?)");
            parameters.add("%" + name.trim() + "%");
        }
        if (staffId != null) {
            where.append(" AND s.StaffID = ?");
            parameters.add(staffId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND s.IsActive = ?");
            parameters.add(Boolean.parseBoolean(status));
        }
        try (Connection connection = DBUtils.getConnection()) {
            int totalItems;
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM Staff s" + where)) {
                bind(statement, parameters);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    totalItems = resultSet.getInt(1);
                }
            }
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
            int page = Math.min(Math.max(1, requestedPage), totalPages);
            List<Staff> items = new ArrayList<>();
            String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID"
                    + where + " ORDER BY s.StaffID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, parameters);
                statement.setInt(parameters.size() + 1, (page - 1) * pageSize);
                statement.setInt(parameters.size() + 2, pageSize);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) items.add(extractStaffFromResultSet(resultSet));
                }
            }
            return new PageResult<>(items, page, pageSize, totalItems);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Cannot search staff", e);
        }
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            if (value instanceof Integer) statement.setInt(i + 1, (Integer) value);
            else if (value instanceof Boolean) statement.setBoolean(i + 1, (Boolean) value);
            else statement.setString(i + 1, String.valueOf(value));
        }
    }    /**
     * Inserts a new staff member into the database.
     *
     * @param staff The Staff object containing the data to be inserted.
     */
    public void createStaff(Staff staff) {
        String sql = "INSERT INTO Staff (StaffCode, FullName, DateOfBirth, Gender, PhoneNumber, Email, "
                + "Password, Department, Position, Salary, HireDate, Role_ID, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getStaffCode());
            ps.setString(2, staff.getFullName());
            ps.setDate(3, toSqlDate(staff.getDateOfBirth()));
            ps.setBoolean(4, staff.isGender());
            ps.setString(5, staff.getPhoneNumber());
            ps.setString(6, staff.getEmail());
            ps.setString(7, staff.getPassword());
            ps.setString(8, staff.getDepartment());
            ps.setString(9, staff.getPosition());
            ps.setBigDecimal(10, staff.getSalary());
            ps.setDate(11, toSqlDate(staff.getHireDate()));
            ps.setInt(12, staff.getRole().getRoleID());
            ps.setBoolean(13, staff.isIsActive());
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /** Converts a {@link LocalDate} to {@link java.sql.Date}, tolerating null. */
    private static Date toSqlDate(LocalDate date) {
        return date != null ? Date.valueOf(date) : null;
    }

    /**
     * Updates an existing staff member's information in the database. The
     * password is not updated via this method.
     *
     * @param staff The Staff object containing the updated data. The StaffID
     * must be set.
     */
    public void updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET StaffCode = ?, FullName = ?, DateOfBirth = ?, Gender = ?, "
                + "PhoneNumber = ?, Email = ?, Department = ?, Position = ?, Salary = ?, HireDate = ?, "
                + "Role_ID = ?, IsActive = ? WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getStaffCode());
            ps.setString(2, staff.getFullName());
            ps.setDate(3, toSqlDate(staff.getDateOfBirth()));
            ps.setBoolean(4, staff.isGender());
            ps.setString(5, staff.getPhoneNumber());
            ps.setString(6, staff.getEmail());
            ps.setString(7, staff.getDepartment());
            ps.setString(8, staff.getPosition());
            ps.setBigDecimal(9, staff.getSalary());
            ps.setDate(10, toSqlDate(staff.getHireDate()));
            ps.setInt(11, staff.getRole().getRoleID());
            ps.setBoolean(12, staff.isIsActive());
            ps.setInt(13, staff.getStaffID());
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a staff member from the database by their ID.
     *
     * @param staffId The ID of the staff member to delete.
     */
    public void deleteStaff(int staffId) {
        String sql = "DELETE FROM Staff WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a single staff member by their unique ID.
     *
     * @param staffId The ID of the staff member to retrieve.
     * @return A populated Staff object if found, null otherwise.
     */
    public Staff getStaffById(int staffId) {
        String sql = "SELECT s.*, r.Role_Name FROM Staff s JOIN Role r ON s.Role_ID = r.Role_ID WHERE s.StaffID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Staff getStaffByEmail(String email) {
        String sql = "SELECT * FROM Staff WHERE Email = ?";

        try (Connection conn = DBUtils.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Staff staff = new Staff();
                    staff.setStaffID(rs.getInt("StaffID"));
                    staff.setStaffCode(rs.getString("StaffCode"));
                    staff.setFullName(rs.getString("FullName"));
                    staff.setEmail(rs.getString("Email"));
                    staff.setPassword(rs.getString("Password"));
                    staff.setIsActive(rs.getBoolean("IsActive"));
                    return staff;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        return null; 
    }

    public void updateActiveStatus(String email, boolean isActive) {
        String sql = "UPDATE Staff SET IsActive = ? WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
