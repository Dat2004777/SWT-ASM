package fu.swt301.sms.system;

import io.github.bonigarcia.wdm.WebDriverManager;
import fu.swt301.sms.system.pages.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationSystemTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private final String BASE_URL = "http://localhost:8080/StaffManagement";

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("ST_LGN_001: Đăng nhập Admin thành công")
    public void testST_LGN_001_LoginSuccessAsAdmin() {
        driver.get(BASE_URL + "/login");
        loginPage.login("admin@example.com", "admin123");

        assertTrue(driver.getCurrentUrl().contains("/staff-list"));
    }

    @Test
    @Order(2)
    @DisplayName("ST_LGN_002: Đăng nhập Staff thành công")
    public void testST_LGN_002_LoginSuccessAsStaff() {
        driver.get(BASE_URL + "/login");
        loginPage.login("user1@example.com", "user123");

        assertTrue(driver.getCurrentUrl().contains("/staff-list"));
    }

    @Test
    @Order(3)
    @DisplayName("ST_LGN_003: Đăng nhập thất bại - Sai mật khẩu")
    public void testST_LGN_003_LoginFailedWrongPassword() {
        driver.get(BASE_URL + "/login");
        loginPage.login("admin@example.com", "wrongpass");

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(loginPage.getErrorMessage().contains("Attempt 1/5"));
    }

    @Test
    @Order(4)
    @DisplayName("ST_LGN_004: Đăng nhập thất bại - Email không tồn tại")
    public void testST_LGN_004_LoginFailedUnregisteredEmail() {
        driver.get(BASE_URL + "/login");
        loginPage.login("notfound@example.com", "admin123");

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(loginPage.getErrorMessage().contains("Invalid email or password"));
    }

    @Test
    @Order(5)
    @DisplayName("ST_LGN_005: Khóa tài khoản (Lockout) sau 5 lần nhập sai")
    public void testST_LGN_005_AccountLockout() {
        driver.get(BASE_URL + "/login");

        for (int i = 0; i < 5; i++) {
            loginPage.login("user2@example.com", "hackpass");
        }

        loginPage.login("user2@example.com", "hackpass");
        String errorMsg = loginPage.getErrorMessage().toLowerCase();
        assertTrue(errorMsg.contains("locked") || errorMsg.contains("try again after"));
    }

    @Test
    @Order(6)
    @DisplayName("ST_ACC_001: Khách vãng lai bị chặn khi vào thẳng trang bảo mật")
    public void testST_ACC_001_GuestUserIntercepted() {
        driver.get(BASE_URL + "/staff-list");

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(7)
    @DisplayName("ST_ACC_002: Tài khoản Staff bị chặn vào link CRUD của Admin")
    public void testST_ACC_002_StaffRestrictedFromAdminPages() {
        driver.get(BASE_URL + "/login");
        loginPage.login("user1@example.com", "user123");

        driver.get(BASE_URL + "/staff-crud");

        String pageSource = driver.getPageSource().toLowerCase();
        assertTrue(driver.getCurrentUrl().contains("/staff-list"));
    }

    @Test
    @Order(8)
    @DisplayName("ST_ACC_003: Đăng xuất xóa sạch Session (Nhấn Back không quay lại được)")
    public void testST_ACC_003_SessionClearanceOnLogout() {
        driver.get(BASE_URL + "/login");
        loginPage.login("admin@example.com", "admin123");

        driver.findElement(By.partialLinkText("Logout")).click();

        driver.navigate().back();

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
