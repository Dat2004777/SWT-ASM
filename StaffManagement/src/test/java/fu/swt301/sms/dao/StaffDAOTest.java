package fu.swt301.sms.dao;

import fu.swt301.sms.entity.PageResult;
import fu.swt301.sms.entity.Role;
import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.utils.DBUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class StaffDAOTest {

    private StaffDAO staffDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DBUtils> mockedDbUtils;

    @BeforeEach
    public void setUp() throws Exception {
        staffDAO = new StaffDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDbUtils = mockStatic(DBUtils.class);
        mockedDbUtils.when(DBUtils::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    public void tearDown() {
        mockedDbUtils.close();
    }

    @Test
    public void testCheckLogin_Success_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("StaffID")).thenReturn(1);
        when(mockResultSet.getString("StaffCode")).thenReturn("EMP01");
        when(mockResultSet.getString("FullName")).thenReturn("Admin User");
        when(mockResultSet.getDate("DateOfBirth")).thenReturn(Date.valueOf(LocalDate.of(1995, 1, 1)));
        when(mockResultSet.getBoolean("Gender")).thenReturn(true);
        when(mockResultSet.getString("PhoneNumber")).thenReturn("0901234567");
        when(mockResultSet.getString("Email")).thenReturn("admin@example.com");
        when(mockResultSet.getString("Department")).thenReturn("IT");
        when(mockResultSet.getString("Position")).thenReturn("Manager");
        when(mockResultSet.getBigDecimal("Salary")).thenReturn(new BigDecimal("20000000"));
        when(mockResultSet.getDate("HireDate")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBoolean("IsActive")).thenReturn(true);
        when(mockResultSet.getString("Password")).thenReturn("any_hashed_password");
        when(mockResultSet.getInt("Role_ID")).thenReturn(1);
        when(mockResultSet.getString("Role_Name")).thenReturn("Admin");

        try (MockedStatic<fu.swt301.sms.utils.PasswordUtils> mockedPasswordUtils = mockStatic(fu.swt301.sms.utils.PasswordUtils.class)) {
            mockedPasswordUtils.when(() -> fu.swt301.sms.utils.PasswordUtils.verifyPassword(anyString(), anyString()))
                    .thenReturn(true);

            Staff result = staffDAO.checkLogin("admin@example.com", "admin123");

            assertNotNull(result);
            assertEquals("Admin User", result.getFullName());
            assertEquals(1, result.getRole().getRoleID());
        }
    }

    @Test
    public void testCheckLogin_Exception_Coverage() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated DB Fail"));

        Staff result = staffDAO.checkLogin("admin@example.com", "admin123");
        assertNull(result);
    }

    @Test
    public void testIsEmailExists_True() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // COUNT(*) > 0

        assertTrue(staffDAO.isEmailExists("test@example.com", 1));
    }

    @Test
    public void testIsFullNameExists_True() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        assertTrue(staffDAO.isFullNameExists("Nguyen Van A", 0));
    }

    @Test
    public void testIsPhoneNumberExists_True() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        assertTrue(staffDAO.isPhoneNumberExists("0901234567", 0));
    }

    @Test
    public void testIsStaffCodeExists_False() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // COUNT(*) = 0

        assertFalse(staffDAO.isStaffCodeExists("ST01", 0));
    }

    @Test
    public void testCreateStaff_Success_Coverage() throws Exception {
        Staff s = new Staff();
        s.setStaffCode("ST99");
        s.setDateOfBirth(LocalDate.of(2000, 1, 1));
        s.setHireDate(null);
        Role r = new Role();
        r.setRoleID(2);
        s.setRole(r);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> staffDAO.createStaff(s));
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateStaff_Success_Coverage() throws Exception {
        Staff s = new Staff();
        s.setStaffID(1);
        s.setRole(new Role());

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> staffDAO.updateStaff(s));
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteStaff_Success_Coverage() throws Exception {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> staffDAO.deleteStaff(1));
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testGetStaffById_NotFound() throws Exception {
        when(mockResultSet.next()).thenReturn(false);

        assertNull(staffDAO.getStaffById(999));
    }

    @Test
    public void testSearch_WithFullCriteria_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(15);

        when(mockResultSet.getInt("StaffID")).thenReturn(5);
        when(mockResultSet.getString("FullName")).thenReturn("Nguyen Van Test");

        PageResult<Staff> result = staffDAO.search("Nguyen", 5, "true", 1, 10);

        assertNotNull(result);
        assertEquals(15, result.getTotalItems());
        assertEquals(2, result.getTotalPages());
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    public void testSearch_WithNullAndEmptyParameters_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(0);

        PageResult<Staff> result = staffDAO.search("   ", null, null, 1, 10);
        assertNotNull(result);
        assertEquals(0, result.getTotalItems());
    }

    @Test
    public void testGetStaffByFilter_Coverage() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("StaffID")).thenReturn(10);
        when(mockResultSet.getString("FullName")).thenReturn("Filter Name");

        List<Staff> list = staffDAO.getStaffByFilter("Filter", "false");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }
}
