package fu.swt301.sms.system;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Khối 2 — System (Selenium) tests for the Staff Management CRUD module
 * (FR-06..FR-10).
 * Eight end-to-end scenarios (ST_CRD_001..008) that drive a real browser
 * against the
 * deployed application, simulating an Admin clicking buttons on the forms.
 *
 * <p>
 * <b>Pre-requisites:</b> the WAR must be running on Tomcat with SQL Server
 * seeded by
 * {@code DataInitializer} (admin@example.com / admin123 and 1000 STAFF users).
 * The base URL
 * defaults to {@code http://localhost:8080/StaffManagement} and can be
 * overridden with
 * {@code -Dapp.base.url=...}. The browser is shown by default; pass
 * {@code -Dselenium.headless=true} to run it hidden (e.g. on CI).
 * If the server is unreachable the whole class is skipped (not failed) so
 * {@code mvn test}
 * stays green when no server is up.
 */
@DisplayName("ST_CRD — Staff Management CRUD (Selenium System Tests)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffCrudSystemTest {

    private static final String BASE_URL = System.getProperty("app.base.url", "http://localhost:8080/StaffManagement");
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    /**
     * Delay (ms) inserted after each UI action so a human can follow along during a
     * demo.
     * Override with {@code -Dselenium.slowmo=1500} (slower) or
     * {@code -Dselenium.slowmo=0}
     * (full speed, e.g. on CI).
     */
    private static final long SLOWMO_MS = Long.getLong("selenium.slowmo", 0L);

    /** Distinguishes records created within the same millisecond across tests. */
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void startBrowser() {
        Assumptions.assumeTrue(isServerUp(),
                "Application not reachable at " + BASE_URL + " — skipping Selenium system tests.");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "false"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--no-sandbox",
                "--disable-dev-shm-usage", "--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterAll
    static void quitBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Every test starts from a fresh Admin session. */
    @BeforeEach
    void loginAsAdmin(TestInfo testInfo) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(">>> DANG CHAY: " + testInfo.getDisplayName());
        System.out.println("==================================================================");
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        wait.until(ExpectedConditions.urlContains("staff-list"));
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_001 — Admin adds a valid staff -> row appears in the table //
    // ------------------------------------------------------------------ //
    @Test
    @Order(1)
    @DisplayName("ST_CRD_001: Add a valid staff -> record appears in the list")
    void st_crd_001_addValidStaff_appearsInTable() {
        String suffix = uniqueSuffix();
        String name = "Selenium Valid " + suffix;

        createStaff(code(suffix), name, phone(suffix), email(suffix),
                "Engineering", "15000000");

        WebElement row = findRowByName(name);
        assertTrue(row.getText().contains(name),
                "Newly created staff should be listed after creation.");
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_002 — Duplicate email -> server-side validation error //
    // ------------------------------------------------------------------ //
    @Test
    @Order(2)
    @DisplayName("ST_CRD_002: Add staff with a duplicate email -> validation error")
    void st_crd_002_duplicateEmail_showsValidationError() {
        String suffix = uniqueSuffix();
        openCreateForm();
        // Unique code & phone so the ONLY uniqueness violation is the email.
        fillStaffForm(code(suffix), "Selenium Dup Email " + suffix,
                phone(suffix), ADMIN_EMAIL, "Engineering", "15000000", "Test@123");
        submitForm();

        WebElement alert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(alert.getText().contains("Email already exists"),
                "Expected a server-side duplicate-email error, got: " + alert.getText());
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_003 — Empty required fields -> blocked client-side //
    // ------------------------------------------------------------------ //
    @Test
    @Order(3)
    @DisplayName("ST_CRD_003: Submit with empty required fields -> blocked by client-side validation")
    void st_crd_003_emptyRequiredFields_blockedClientSide() {
        openCreateForm();
        submitForm(); // submit the empty form

        WebElement staffCode = driver.findElement(By.id("staffCode"));
        assertFalse(isValid(staffCode),
                "Empty required Staff Code should fail HTML5 validation.");
        assertTrue(driver.getCurrentUrl().contains("staff-crud"),
                "Form must not navigate away while required fields are empty.");
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_004 — View Details shows the correct staff info (FR-10) //
    // ------------------------------------------------------------------ //
    @Test
    @Order(4)
    @DisplayName("ST_CRD_004: View Details shows the correct Salary & Department")
    void st_crd_004_viewDetails_showsCorrectInfo() {
        String suffix = uniqueSuffix();
        String name = "Selenium View " + suffix;
        String department = "Dept-" + suffix;
        String salary = "17000000";

        createStaff(code(suffix), name, phone(suffix), email(suffix), department, salary);

        WebElement row = findRowByName(name);
        click(row.findElement(By.xpath(".//a[contains(@href,'action=view')]")));

        WebElement body = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String text = body.getText();
        assertTrue(text.contains(name), "Detail page should show the staff name.");
        assertTrue(text.contains(department), "Detail page should show the Department.");
        assertTrue(text.replace(",", "").contains(salary),
                "Detail page should show the Salary. Page text: " + text);
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_005 — Edit phone number -> table reflects the change //
    // ------------------------------------------------------------------ //
    @Test
    @Order(5)
    @DisplayName("ST_CRD_005: Edit phone number -> Update -> list shows new value")
    void st_crd_005_editPhone_updatesTable() {
        String suffix = uniqueSuffix();
        String name = "Selenium Edit " + suffix;
        createStaff(code(suffix), name, phone(suffix), email(suffix), "Engineering", "15000000");

        // Open the Edit form for this exact staff.
        click(findRowByName(name).findElement(By.xpath(".//a[contains(@href,'action=edit')]")));

        String newPhone = phone(uniqueSuffix());
        WebElement phoneField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("phoneNumber")));
        phoneField.clear();
        phoneField.sendKeys(newPhone);
        submitForm();

        wait.until(ExpectedConditions.urlContains("staff-list"));
        WebElement row = findRowByName(name);
        assertTrue(row.getText().contains(newPhone),
                "Updated phone number should appear in the list row.");
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_006 — Negative salary on edit -> update rejected //
    // ------------------------------------------------------------------ //
    @Test
    @Order(6)
    @DisplayName("ST_CRD_006: Edit salary to a negative value -> rejected")
    void st_crd_006_negativeSalary_rejected() {
        String suffix = uniqueSuffix();
        String name = "Selenium NegSalary " + suffix;
        createStaff(code(suffix), name, phone(suffix), email(suffix), "Engineering", "15000000");

        click(findRowByName(name).findElement(By.xpath(".//a[contains(@href,'action=edit')]")));

        WebElement salary = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("salary")));
        salary.clear();
        salary.sendKeys("-5000");
        submitForm();

        assertFalse(isValid(salary),
                "Negative salary (below min=1000000) must fail validation.");
        assertTrue(driver.getCurrentUrl().contains("staff-crud"),
                "Form must not submit a negative salary.");
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_007 — Delete a staff -> that account can no longer log in //
    // ------------------------------------------------------------------ //
    @Test
    @Order(7)
    @DisplayName("ST_CRD_007: Delete a staff -> the account cannot log in anymore")
    void st_crd_007_deletedStaff_cannotLogin() {
        String suffix = uniqueSuffix();
        String name = "Selenium Delete " + suffix;
        String loginEmail = email(suffix);
        String password = "Test@123";
        createStaff(code(suffix), name, phone(suffix), loginEmail, "Engineering", "15000000", password);

        // Delete via the row's Delete button (confirm() dialog handled below).
        WebElement deleteLink = findRowByName(name).findElement(By.xpath(".//a[contains(@href,'action=delete')]"));
        click(deleteLink);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (TimeoutException ignored) {
            // Some headless builds auto-confirm navigation; the delete still fires.
        }
        wait.until(ExpectedConditions.urlContains("staff-list"));

        // The deleted account must now fail authentication.
        driver.get(BASE_URL + "/logout");
        login(loginEmail, password);

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(error.isDisplayed(), "Deleted account should be rejected at login.");
        assertTrue(driver.getCurrentUrl().contains("login"),
                "A failed login must stay on the login page, not reach the staff list.");
    }

    // ------------------------------------------------------------------ //
    // ST_CRD_008 — Invalid date-of-birth format -> data error //
    // ------------------------------------------------------------------ //
    @Test
    @Order(8)
    @DisplayName("ST_CRD_008: Invalid date of birth (e.g. 31/02/2000) -> data error")
    void st_crd_008_invalidDateOfBirth_reportsError() {
        String suffix = uniqueSuffix();
        openCreateForm();
        // Fill every field validly first, so the date is the only blocker.
        fillStaffForm(code(suffix), "Selenium BadDate " + suffix,
                phone(suffix), email(suffix), "Engineering", "15000000", "Test@123");

        // An impossible date: the <input type="date"> rejects it (value becomes
        // empty/invalid).
        WebElement dob = driver.findElement(By.id("dateOfBirth"));
        jsSetValue(dob, "2000-02-31");
        submitForm();

        assertFalse(isValid(dob),
                "An impossible date of birth must not be accepted by the date field.");
        assertTrue(driver.getCurrentUrl().contains("staff-crud"),
                "Form must not submit with an invalid date of birth.");
    }

    // =================================================================== //
    // Helpers //
    // =================================================================== //

    private void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        pause();
        type(By.id("email"), email);
        type(By.id("password"), password);
        click(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    private void openCreateForm() {
        driver.get(BASE_URL + "/staff-crud?action=create");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("staffCode")));
        pause();
    }

    /** Creates a staff with the default password through the Add Staff form. */
    private void createStaff(String code, String name, String phone, String email,
            String department, String salary) {
        createStaff(code, name, phone, email, department, salary, "Test@123");
    }

    private void createStaff(String code, String name, String phone, String email,
            String department, String salary, String password) {
        openCreateForm();
        fillStaffForm(code, name, phone, email, department, salary, password);
        submitForm();
        wait.until(ExpectedConditions.urlContains("staff-list"));
    }

    /** Fills the staff form. The password field only exists on the create form. */
    private void fillStaffForm(String code, String name, String phone, String email,
            String department, String salary, String password) {
        type(By.id("staffCode"), code);
        type(By.id("fullName"), name);
        jsSetValue(driver.findElement(By.id("dateOfBirth")), "2000-01-01");
        click(driver.findElement(By.id("male")));
        type(By.id("phoneNumber"), phone);
        type(By.id("email"), email);
        type(By.id("department"), department);
        type(By.id("position"), "Engineer");
        type(By.id("salary"), salary);
        jsSetValue(driver.findElement(By.id("hireDate")), "2023-01-01");

        var passwordFields = driver.findElements(By.id("password"));
        if (!passwordFields.isEmpty()) {
            passwordFields.get(0).clear();
            passwordFields.get(0).sendKeys(password);
            pause();
        }
        new Select(driver.findElement(By.id("roleID"))).selectByVisibleText("Staff");
        pause();
        click(driver.findElement(By.id("active")));
    }

    private void submitForm() {
        click(driver.findElement(By.cssSelector("button[type='submit']")));
    }

    /**
     * Navigates to the list filtered by name and returns the single matching row.
     */
    private WebElement findRowByName(String name) {
        driver.get(BASE_URL + "/staff-list?searchName="
                + URLEncoder.encode(name, StandardCharsets.UTF_8));
        String rowXpath = "//tbody/tr[td[2][normalize-space()='" + name + "']]";
        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(rowXpath)));
        pause();
        return row;
    }

    private void type(By locator, String value) {
        WebElement el = driver.findElement(locator);
        el.clear();
        if (SLOWMO_MS <= 0) {
            el.sendKeys(value);
        } else {
            // Type one character at a time so the input is visible during a demo.
            long perChar = Math.min(120, Math.max(20, SLOWMO_MS / 8));
            for (char c : value.toCharArray()) {
                el.sendKeys(String.valueOf(c));
                sleep(perChar);
            }
        }
        pause();
    }

    private void jsSetValue(WebElement el, String value) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", el, value);
        pause();
    }

    /** Clicks an element, then pauses so the resulting page change is watchable. */
    private void click(WebElement el) {
        el.click();
        pause();
    }

    /** Pauses for the configured slow-motion delay between UI actions. */
    private void pause() {
        sleep(SLOWMO_MS);
    }

    private static void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isValid(WebElement el) {
        return Boolean.TRUE.equals(
                ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", el));
    }

    // --- unique-data generators (respect the form's validation rules) --- //

    private static String uniqueSuffix() {
        return (System.currentTimeMillis() % 100000) + "" + SEQ.incrementAndGet();
    }

    private static String code(String suffix) {
        return "ST" + suffix; // [A-Za-z0-9]{3,20}
    }

    private static String email(String suffix) {
        return "sel" + suffix + "@example.com";
    }

    private static String phone(String suffix) {
        // 10 digits, starts with 0.
        long n = Long.parseLong(suffix) % 1_000_000_000L;
        return "0" + String.format("%09d", n);
    }

    private static boolean isServerUp() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + "/login").openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
