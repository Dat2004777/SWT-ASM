/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package fu.swt301.sms.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author dat20
 */
public class PasswordUtilsTest {

    @Test
    public void testHashPassword_ShouldReturnValidBCryptString() {
        String plainText = "admin123";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertEquals(60, hashedPassword.length());
    }

    @Test
    public void testHashPassword_Boundary_SingleCharacter() {
        String plainText = "a";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertEquals(60, hashedPassword.length());
    }

    @Test
    public void testHashPassword_ComplexSymbols() {
        String plainText = "P@ss!26";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertEquals(60, hashedPassword.length());
    }

    @Test
    public void testHashPassword_Abnormal_EmptyString() {
        String plainText = "";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertEquals(60, hashedPassword.length());
    }

    @Test
    public void testVerifyPassword_ShouldReturnTrue_WhenPasswordMatches() {
        String plainText = "admin123";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        boolean isMatched = PasswordUtils.verifyPassword(plainText, hashedPassword);
        assertTrue(isMatched);
    }

    @Test
    public void testVerifyPassword_ShouldReturnFalse_WhenPasswordIsIncorrect() {
        String plainText = "admin123";
        String wrongText = "wrong123";
        String hashedPassword = PasswordUtils.hashPassword(plainText);

        boolean isMatched = PasswordUtils.verifyPassword(wrongText, hashedPassword);
        assertFalse(isMatched);
    }

    @Test
    public void testVerifyPassword_InvalidHashFormat_ShouldReturnFalse() {
        boolean isMatched = PasswordUtils.verifyPassword("admin123", "invalid_hash_string");
        assertFalse(isMatched);
    }

}
