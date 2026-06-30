package fu.swt301.sms.config;

import fu.swt301.sms.utils.DBUtils;
import fu.swt301.sms.utils.PasswordUtils;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * This listener class is automatically instantiated and invoked by the web
 * container when the application starts up. Its primary purpose is to
 * initialize the database by: 1. Creating the necessary tables ('Role',
 * 'Staff') if they do not already exist. 2. Seeding the tables with default
 * data (e.g., user roles and a default admin account) if they are empty. This
 * makes the application self-contained and easier to deploy.
 */
@WebListener
public class DataInitializer implements ServletContextListener {

    /**
     * This method is called by the container when the web application is first
     * started. It orchestrates the database initialization process.
     *
     * @param sce The event object containing the ServletContext.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DBUtils.getConnection()) {
            // Step 1: Ensure database tables are created before proceeding.
            System.out.println("Checking database schema...");
            createRoleTableIfNotExists(conn);
            createStaffTableIfNotExists(conn);

            // Step 2: Check if the 'Role' table is empty. If it is, we assume the database is new and needs seeding.
            boolean dataExists = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Role"); ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    dataExists = true;
                }
            }

            // Step 3: If no data exists, insert the default roles and a default admin user.
            if (!dataExists) {
                System.out.println("No data found. Initializing default data...");
                insertDefaultData(conn);
            } else {
                System.out.println("Data already exists. Skipping initialization.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            // If any database error occurs during initialization, log it and throw a RuntimeException
            // to halt the application's startup, as it cannot function without a proper database setup.
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database.", e);
        }
    }

    /**
     * Checks if the 'Role' table exists in the database. If not, it creates the
     * table.
     *
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void createRoleTableIfNotExists(Connection conn) throws SQLException {
        String tableName = "Role";
        String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        boolean tableExists = false;

        try (PreparedStatement ps = conn.prepareStatement(checkTableSQL)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
        }

        if (!tableExists) {
            System.out.println("Table 'Role' not found. Creating table...");
            String createSQL = "CREATE TABLE Role ("
                    + "Role_ID INT PRIMARY KEY, "
                    + "Role_Name NVARCHAR(50) NOT NULL UNIQUE"
                    + ")";
            try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
                ps.execute();
                System.out.println("Table 'Role' created.");
            }
        }
    }

    /**
     * Checks if the 'Staff' table exists in the database. If not, it creates
     * the table with a foreign key constraint pointing to the 'Role' table.
     *
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void createStaffTableIfNotExists(Connection conn) throws SQLException {
        String tableName = "Staff";
        String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        boolean tableExists = false;

        try (PreparedStatement ps = conn.prepareStatement(checkTableSQL)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableExists = true;
                }
            }
        }

        if (!tableExists) {
            System.out.println("Table 'Staff' not found. Creating table...");
            String createSQL = "CREATE TABLE Staff ("
                    + "StaffID INT PRIMARY KEY IDENTITY(1,1), "
                    + "StaffCode VARCHAR(20) NOT NULL UNIQUE, "
                    + "FullName NVARCHAR(100) NOT NULL, "
                    + "DateOfBirth DATE NULL, "
                    + "Gender BIT NOT NULL, "
                    + "PhoneNumber VARCHAR(20), "
                    + "Email VARCHAR(100) NOT NULL UNIQUE, "
                    + "Password VARCHAR(255) NOT NULL, "
                    + "Department NVARCHAR(100) NULL, "
                    + "Position NVARCHAR(100) NULL, "
                    + "Salary DECIMAL(18,2) NULL, "
                    + "HireDate DATE NULL, "
                    + "Role_ID INT NOT NULL, "
                    + "IsActive BIT NOT NULL, "
                    + "CONSTRAINT FK_Staff_Role FOREIGN KEY (Role_ID) REFERENCES Role(Role_ID)"
                    + ")";
            try (PreparedStatement ps = conn.prepareStatement(createSQL)) {
                ps.execute();
                System.out.println("Table 'Staff' created.");
            }
        }

        // For databases created before FR-07, add the new columns if they are missing
        // so the application keeps running without a manual migration.
        addColumnIfNotExists(conn, "StaffCode", "VARCHAR(20)");
        addColumnIfNotExists(conn, "DateOfBirth", "DATE");
        addColumnIfNotExists(conn, "Department", "NVARCHAR(100)");
        addColumnIfNotExists(conn, "Position", "NVARCHAR(100)");
        addColumnIfNotExists(conn, "Salary", "DECIMAL(18,2)");
        addColumnIfNotExists(conn, "HireDate", "DATE");
    }

    /**
     * Adds a column to the 'Staff' table when it does not already exist. New
     * columns are added as NULL so existing rows remain valid.
     *
     * @param conn The active database connection.
     * @param columnName The column to add.
     * @param columnType The SQL type definition for the column.
     * @throws SQLException if a database access error occurs.
     */
    private void addColumnIfNotExists(Connection conn, String columnName, String columnType) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_NAME = 'Staff' AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSQL)) {
            ps.setString(1, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }
        String alterSQL = "ALTER TABLE Staff ADD " + columnName + " " + columnType + " NULL";
        try (PreparedStatement ps = conn.prepareStatement(alterSQL)) {
            ps.execute();
            System.out.println("Added column '" + columnName + "' to 'Staff'.");
        }
    }

    /**
     * Inserts a predefined set of data into the 'Role' and 'Staff' tables. This
     * includes 'Admin' and 'Staff' roles, and a default administrator account.
     *
     * @param conn The active database connection.
     * @throws SQLException if a database access error occurs.
     */
    private void insertDefaultData(Connection conn) throws SQLException {
        // Insert default roles using a batch operation for efficiency.
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Role (Role_ID, Role_Name) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setString(2, "Admin");
            ps.addBatch();

            ps.setInt(1, 2);
            ps.setString(2, "Staff");
            ps.addBatch();

            ps.executeBatch();
            System.out.println("Default roles inserted.");
        }

        String seedSql = "INSERT INTO Staff (StaffCode, FullName, DateOfBirth, Gender, PhoneNumber, Email, "
                + "Password, Department, Position, Salary, HireDate, Role_ID, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Insert a default administrator user for initial login (password hashed with jBCrypt).
        try (PreparedStatement ps = conn.prepareStatement(seedSql)) {
            ps.setString(1, "ADMIN001");
            ps.setString(2, "Admin User");
            ps.setDate(3, Date.valueOf(LocalDate.of(1990, 1, 1)));
            ps.setBoolean(4, true); // true for Male
            ps.setString(5, "0123456789");
            ps.setString(6, "admin@example.com");
            ps.setString(7, PasswordUtils.hashPassword("admin123"));
            ps.setString(8, "Management");
            ps.setString(9, "Administrator");
            ps.setBigDecimal(10, new BigDecimal("20000000"));
            ps.setDate(11, Date.valueOf(LocalDate.of(2020, 1, 1)));
            ps.setInt(12, 1); // Role_ID for Admin
            ps.setBoolean(13, true); // IsActive
            ps.executeUpdate();
            System.out.println("Default admin user inserted.");
        }

        // A regular Staff account (Role_ID = 2) for testing the read-only role.
        // And create 1000 user record 
        String hashedPassowrd = PasswordUtils.hashPassword("user123");

        try (PreparedStatement ps = conn.prepareStatement(seedSql)) {

            conn.setAutoCommit(false);

            for (int i = 1; i <= 1000; i++) {
                ps.setString(1, "STAFF" + i);
                ps.setString(2, "Nguyen Van A" + i);
                ps.setDate(3, Date.valueOf(LocalDate.of(1998, 6, 15)));
                ps.setBoolean(4, true);
                ps.setString(5, "0987654" + String.format("%03d", i));
                ps.setString(6, "user" + i + "@example.com");
                ps.setString(7, hashedPassowrd);
                ps.setString(8, "Human Resources");
                ps.setString(9, "HR Staff");
                ps.setBigDecimal(10, new BigDecimal("12000000"));
                ps.setDate(11, Date.valueOf(LocalDate.of(2022, 3, 1)));
                ps.setInt(12, 2); // Role_ID = 2 (Staff)
                ps.setBoolean(13, true);

                ps.addBatch();

                if (i % 200 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
            System.out.println("Default 1000 staff user inserted.");
        } catch (SQLException e) {
            conn.rollback();
            e.printStackTrace();
        }
    }

    /**
     * This method is called by the container when the web application is about
     * to be shut down. No cleanup action is needed in this case.
     *
     * @param sce The event object containing the ServletContext.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No action needed on shutdown.
    }
}
