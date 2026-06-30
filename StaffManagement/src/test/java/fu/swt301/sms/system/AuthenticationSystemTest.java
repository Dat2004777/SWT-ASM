package fu.swt301.sms.system;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSystemTest {

    private WebDriver driver;
    private final String BASE_URL = "http://localhost:8080/StaffManagement";

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit(); 
        }
    }

    @Test
    public void testST_LGN_001_LoginSuccessAsAdmin() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("email")).sendKeys("admin@example.com");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/staff-list"), "Chưa chuyển hướng đến trang danh sách nhân viên");
    }

    @Test
    public void testST_LGN_003_LoginFailed_ShowAttemptCounter() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("email")).sendKeys("admin@example.com");
        driver.findElement(By.name("password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorElement = driver.findElement(By.className("alert"));
        String errorText = errorElement.getText();

        assertTrue(errorText.contains("Attempt 1/5"), "Thông báo đếm số lần sai hiển thị không đúng");
    }

    @Test
    public void testST_ACC_001_GuestUserRedirectToLogin() {
        driver.get(BASE_URL + "/staff-list");

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"), "Chưa chặn được Guest quay về trang login");
    }
}
