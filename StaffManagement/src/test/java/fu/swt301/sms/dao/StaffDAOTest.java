/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.dao;

import fu.swt301.sms.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author dat20
 */
public class StaffDAOTest {

    private StaffDAO staffDAO;

    @BeforeEach
    public void setUp() {
        staffDAO = new StaffDAO();
    }

    @Test
    public void testCheckLogin_Success_WhenCredentialsAreValid() {
        String email = "admin@example.com";
        String password = "admin123";

        Staff loggedInStaff = staffDAO.checkLogin(email, password);

        assertNotNull(loggedInStaff);
        assertEquals("Admin User", loggedInStaff.getFullName());
        assertNotNull(loggedInStaff.getRole());
        assertEquals(1, loggedInStaff.getRole().getRoleID());
    }

    @Test
    public void testCheckLogin_Failed_WhenPasswordIsIncorrect() {
        String email = "admin@example.com";
        String wrongPassword = "wrongpassword";

        Staff loggedInStaff = staffDAO.checkLogin(email, wrongPassword);

        assertNull(loggedInStaff);
    }

    @Test
    public void testCheckLogin_Failed_WhenEmailDoesNotExist() {
        String invalidEmail = "nonexistent@example.com";
        String password = "admin123";

        Staff loggedInStaff = staffDAO.checkLogin(invalidEmail, password);

        assertNull(loggedInStaff);
    }

}
