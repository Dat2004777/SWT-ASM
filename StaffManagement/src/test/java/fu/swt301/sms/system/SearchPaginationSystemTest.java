package fu.swt301.sms.system;

import fu.swt301.sms.system.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchPaginationSystemTest {

    private static final String BASE_URL = "http://localhost:8080/StaffManagement";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void loginAsAdmin() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        driver.get(BASE_URL + "/login");
        new LoginPage(driver).login("admin@example.com", "admin123");
        wait.until(ExpectedConditions.urlContains("/staff-list"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("ST_STH_001: Search Nguyen only displays matching staff")
    void testST_STH_001_SearchExistingName() {
        searchByName("Nguyen");

        List<WebElement> names = driver.findElements(By.cssSelector("table tbody tr td:nth-child(2)"));
        assertFalse(names.isEmpty(), "Expected at least one search result");
        assertTrue(names.stream().allMatch(name -> name.getText().toLowerCase().contains("nguyen")),
                "Every displayed staff name must contain Nguyen");
    }

    @Test
    @DisplayName("ST_STH_002: Unknown keyword displays No staff found")
    void testST_STH_002_SearchUnknownName() {
        searchByName("keyword-that-does-not-exist-987654321");

        WebElement emptyMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("table tbody td.text-muted")));
        assertTrue(emptyMessage.getText().contains("No staff found"));
    }

    @Test
    @DisplayName("ST_PAG_001: Clicking page 2 loads the next staff page")
    void testST_PAG_001_OpenSecondPage() {
        List<String> firstPageIds = visibleStaffIds();
        WebElement pageTwo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'pagination')]//a[normalize-space()='2']")));
        pageTwo.click();

        wait.until(ExpectedConditions.urlContains("page=2"));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//ul[contains(@class,'pagination')]//li[contains(@class,'active')]/a[normalize-space()='2']")));
        List<String> secondPageIds = visibleStaffIds();

        assertFalse(secondPageIds.isEmpty(), "Page 2 must contain staff records");
        assertFalse(secondPageIds.equals(firstPageIds), "Page 2 must differ from page 1");
    }

    @Test
    @DisplayName("ST_PAG_002: Previous is disabled on page 1")
    void testST_PAG_002_PreviousDisabledOnFirstPage() {
        WebElement previousItem = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//ul[contains(@class,'pagination')]//a[normalize-space()='Previous']/parent::li")));

        assertTrue(previousItem.getAttribute("class").contains("disabled"),
                "Previous must be disabled on page 1");
    }

    private void searchByName(String keyword) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("searchName")));
        input.clear();
        input.sendKeys(keyword);
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[.//input[@name='searchName']]//button[normalize-space()='Search']")));
        searchButton.click();
        wait.until(ExpectedConditions.attributeToBe(By.name("searchName"), "value", keyword));
    }

    private List<String> visibleStaffIds() {
        return driver.findElements(By.cssSelector("table tbody tr td:first-child"))
                .stream()
                .map(WebElement::getText)
                .toList();
    }
}
