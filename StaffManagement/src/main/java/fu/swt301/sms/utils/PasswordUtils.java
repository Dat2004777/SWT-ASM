/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fu.swt301.sms.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author dat20
 */
public class PasswordUtils {
    /**
     * Hash a plain text password using jBCrypt.
     * @param plainPassword The plain text password to hash.
     * @return The hashed password string.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify a plain text password against a hashed password.
     * @param plainPassword The plain text password to check.
     * @param hashedPassword The hashed password from the database.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
