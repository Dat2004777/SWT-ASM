package fu.swt301.sms.integration;

import fu.swt301.sms.dao.StaffDAO;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.DBUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INTEGRATION TEST — verifies that {@link StaffDAO#createStaff(Staff)} actually
 * writes a correct row to the real database (SQL Server via {@link DBUtils}).
 *
 * <p>Unlike a unit test, nothing is mocked here: we exercise the DAO, the JDBC
 * layer and the real {@code Staff} table together. The flow is:
 * <ol>
 *   <li>Build a {@link Staff} with a unique StaffCode / Email / Phone.</li>
 *   <li>Call {@code createStaff(...)} — the method under test.</li>
 *   <li>Read the row back straight from the DB and assert every column matches
 *       what we inserted.</li>
 * </ol>
 *
 * <p>Because {@code createStaff} returns {@code void} (it does not hand back the
 * generated identity key), we first look the new row up by its unique StaffCode
 * to obtain its StaffID, then re-load it through {@link StaffDAO#getStaffById(int)}.
 * The {@code Password} column is checked with a direct query because
 * {@code getStaffById} intentionally does not map the password field.
 *
 * <p>The test cleans up after itself in {@link #cleanUp()} so it can be run
 * repeatedly. If the database is unreachable the whole class is skipped
 * (aborted assumption) instead of failing, so {@code mvn test} stays green on a
 * machine without SQL Server.
 */
@DisplayName("IT — StaffDAO.createStaff persists a record to the database correctly")
public class StaffCreateDaoIntegrationTest {

    private StaffDAO staffDAO;

    /** A Role_ID that really exists in the Role table (needed for the FK). */
    private static int existingRoleId;

    /** StaffID of the row created by a test, used for cleanup. */
    private Integer insertedStaffId;

    @BeforeAll
    static void assumeDatabaseReachable() {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT TOP 1 Role_ID FROM Role ORDER BY Role_ID");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                existingRoleId = rs.getInt("Role_ID");
            } else {
                Assumptions.abort("No Role rows found — cannot run integration test.");
            }
        } catch (Exception e) {
            Assumptions.abort("Database not reachable, skipping integration test: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        staffDAO = new StaffDAO();
    }

    @AfterEach
    void cleanUp() {
        if (insertedStaffId != null) {
            staffDAO.deleteStaff(insertedStaffId);
            insertedStaffId = null;
        }
    }

    @Test
    @DisplayName("createStaff writes all fields to the DB exactly as provided")
    void createStaff_persistsAllFieldsCorrectly() {
        // --- Arrange: build a staff record with unique business keys ---
        long suffix = System.currentTimeMillis() % 1_000_000L;
        String staffCode = "IT" + suffix;
        String fullName = "IT Tester " + suffix;
        String email = "it" + suffix + "@example.com";
        String phone = "0" + String.format("%09d", suffix);
        LocalDate dob = LocalDate.of(1995, 5, 20);
        LocalDate hireDate = LocalDate.of(2024, 1, 15);
        BigDecimal salary = new BigDecimal("13500000");
        String hashedPassword = "$2a$10$abcdefghijklmnopqrstuv"; // stored as-is by the DAO

        Role role = new Role();
        role.setRoleID(existingRoleId);

        Staff input = new Staff();
        input.setStaffCode(staffCode);
        input.setFullName(fullName);
        input.setDateOfBirth(dob);
        input.setGender(true);
        input.setPhoneNumber(phone);
        input.setEmail(email);
        input.setPassword(hashedPassword);
        input.setDepartment("Integration");
        input.setPosition("Tester");
        input.setSalary(salary);
        input.setHireDate(hireDate);
        input.setRole(role);
        input.setIsActive(true);

        // --- Act: call the method under test ---
        staffDAO.createStaff(input);

        // --- Assert 1: a row with our unique StaffCode now exists ---
        int newId = findStaffIdByCode(staffCode);
        insertedStaffId = newId; // register for cleanup
        assertTrue(newId > 0, "createStaff should have inserted a row retrievable by StaffCode");

        // --- Assert 2: read it back through the DAO and check every column ---
        Staff persisted = staffDAO.getStaffById(newId);
        assertNotNull(persisted, "The inserted staff should be retrievable by id");
        assertEquals(staffCode, persisted.getStaffCode());
        assertEquals(fullName, persisted.getFullName());
        assertEquals(dob, persisted.getDateOfBirth());
        assertTrue(persisted.isGender());
        assertEquals(phone, persisted.getPhoneNumber());
        assertEquals(email, persisted.getEmail());
        assertEquals("Integration", persisted.getDepartment());
        assertEquals("Tester", persisted.getPosition());
        assertEquals(0, salary.compareTo(persisted.getSalary()), "Salary must match exactly");
        assertEquals(hireDate, persisted.getHireDate());
        assertEquals(existingRoleId, persisted.getRole().getRoleID());
        assertTrue(persisted.isIsActive());

        // --- Assert 3: the Password column was written (getStaffById doesn't map it) ---
        assertEquals(hashedPassword, readPasswordById(newId),
                "The password column should be stored exactly as passed to the DAO");
    }

    /** Direct JDBC lookup of the generated StaffID by the unique StaffCode. */
    private int findStaffIdByCode(String staffCode) {
        String sql = "SELECT StaffID FROM Staff WHERE StaffCode = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("StaffID") : -1;
            }
        } catch (Exception e) {
            fail("Could not query StaffID by code: " + e.getMessage());
            return -1; // unreachable
        }
    }

    /** Direct JDBC read of the Password column (not exposed by getStaffById). */
    private String readPasswordById(int staffId) {
        String sql = "SELECT Password FROM Staff WHERE StaffID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Password") : null;
            }
        } catch (Exception e) {
            fail("Could not read Password by id: " + e.getMessage());
            return null; // unreachable
        }
    }
}
