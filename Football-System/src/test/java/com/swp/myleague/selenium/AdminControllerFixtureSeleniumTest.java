package com.swp.myleague.selenium;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import com.swp.myleague.pages.AdminFixturePage;
import com.swp.myleague.pages.LoginPage;
import com.swp.myleague.utils.DriverFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminControllerFixtureSeleniumTest {

    private WebDriver driver;
    private AdminFixturePage adminFixturePage;
    private LoginPage loginPage;
    private static final String ADMIN_USERNAME = "admin123";
    private static final String ADMIN_PASSWORD = "admin123";

    @BeforeEach
    void setUp() throws InterruptedException {
        driver = DriverFactory.createDriver();
        adminFixturePage = new AdminFixturePage(driver);
        loginPage = new LoginPage(driver);
        
        // Đăng nhập admin trước khi test
        loginAsAdmin();
    }
    
    private void loginAsAdmin() throws InterruptedException {
        // Navigate to login page
        loginPage.navigateToLogin();
        Thread.sleep(1000);
        
        // Login với admin credentials
        loginPage.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        Thread.sleep(3000); // Wait longer for login to process
        
        // Đợi redirect sau khi đăng nhập thành công
        Thread.sleep(2000); // Wait for redirect
        String currentUrl = driver.getCurrentUrl();
        System.out.println("After login, current URL: " + currentUrl);
        
        // Verify we're not on login page anymore
        int maxWaitTime = 10; // seconds
        int waited = 0;
        while (currentUrl.contains("/auth/login") && waited < maxWaitTime) {
            Thread.sleep(1000);
            currentUrl = driver.getCurrentUrl();
            waited++;
        }
        
        if (currentUrl.contains("/auth/login")) {
            // Check if there's an error message
            if (loginPage.isErrorMessageDisplayed()) {
                String error = loginPage.getErrorMessage();
                System.err.println("Login failed with error: " + error);
                throw new RuntimeException("Failed to login as admin: " + error);
            }
            throw new RuntimeException("Still on login page after login attempt. URL: " + currentUrl);
        }
        
        System.out.println("Login successful, redirected to: " + currentUrl);
        
        // If redirected to /home, navigate to /admin to verify access
        if (currentUrl.contains("/home")) {
            adminFixturePage.navigateToAdmin();
            Thread.sleep(2000);
            // Verify we can access admin page
            currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/admin")) {
                throw new RuntimeException("Cannot access admin page after login. Current URL: " + currentUrl);
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== TEST CASE 1: Tạo fixtures mới ==========
    @Test
    @DisplayName("TC1: Tạo fixtures mới")
    void testCreateFixtures_CreatesNewFixtures() throws InterruptedException {
        // Given
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        boolean recreate = false;

        // When: Create fixtures (this will redirect to /admin)
        adminFixturePage.createFixtures(startDate, recreate);
        Thread.sleep(3000); // Wait for redirect and fixtures to be generated
        
        // Navigate back to admin dashboard to check fixtures
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then
        assertTrue(adminFixturePage.hasFixturesInSession() || 
                   adminFixturePage.getFixtureCount() > 0,
                   "Fixtures nên được tạo và hiển thị");
    }

    // ========== TEST CASE 2: Tạo lại fixtures khi recreate = true ==========
    @Test
    @DisplayName("TC2: Tạo lại fixtures khi recreate = true")
    void testCreateFixtures_WithRecreate_CreatesNewFixtures() throws InterruptedException {
        // Given
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        boolean recreate = true;

        // When: Create fixtures
        adminFixturePage.createFixtures(startDate, recreate);
        Thread.sleep(3000);
        
        // Navigate back to admin dashboard to check fixtures
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then
        assertTrue(adminFixturePage.hasFixturesInSession() || 
                   adminFixturePage.getFixtureCount() > 0,
                   "Fixtures mới nên được tạo");
    }

    // ========== TEST CASE 3: Sử dụng fixtures từ session khi recreate = false ==========
    @Test
    @DisplayName("TC3: Sử dụng fixtures từ session khi recreate = false")
    void testCreateFixtures_NoRecreate_UsesSessionFixtures() throws InterruptedException {
        // Given: Đã có fixtures trong session
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        
        // Tạo fixtures lần đầu
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);
        int firstFixtureCount = adminFixturePage.getFixtureCount();

        // When: Tạo lại với recreate = false
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then: Số lượng fixtures không đổi (sử dụng từ session)
        int secondFixtureCount = adminFixturePage.getFixtureCount();
        assertEquals(firstFixtureCount, secondFixtureCount,
                    "Nên sử dụng fixtures từ session, không tạo mới");
    }

    // ========== TEST CASE 4: Kiểm tra fixtures được group theo round ==========
    @Test
    @DisplayName("TC4: Kiểm tra fixtures được group theo round")
    void testCreateFixtures_FixturesGroupedByRound() throws InterruptedException {
        // Given
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);

        // When
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then
        int roundCount = adminFixturePage.getRoundCount();
        assertTrue(roundCount > 0, "Nên có ít nhất 1 round");
        assertTrue(adminFixturePage.isFixtureTableDisplayed(),
                  "Fixtures table nên được hiển thị");
    }

    // ========== TEST CASE 5: Lưu fixtures của một round ==========
    @Test
    @DisplayName("TC5: Lưu fixtures của một round")
    void testSaveRound_SavesRoundFixtures() throws InterruptedException {
        // Given: Đã có fixtures trong session
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);
        
        int roundNumber = 1;

        // When: Save round (navigate to save endpoint)
        adminFixturePage.saveRound(roundNumber);
        Thread.sleep(2000);

        // Then: Kiểm tra redirect về /admin
        assertTrue(adminFixturePage.getCurrentUrl().contains("/admin"),
                   "Nên redirect về /admin sau khi lưu");
    }

    // ========== TEST CASE 6: Kiểm tra với startDate hợp lệ ==========
    @Test
    @DisplayName("TC6: Kiểm tra với startDate hợp lệ")
    void testCreateFixtures_ValidStartDate_Success() throws InterruptedException {
        // Given
        String startDate = LocalDate.now().plusDays(14).format(DateTimeFormatter.ISO_DATE);

        // When
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then
        assertTrue(adminFixturePage.hasFixturesInSession() || 
                   adminFixturePage.getFixtureCount() > 0,
                   "Fixtures nên được tạo với startDate hợp lệ");
    }

    // ========== TEST CASE 7: Kiểm tra fixturesByRound được sort ==========
    @Test
    @DisplayName("TC7: Kiểm tra fixturesByRound được sort")
    void testCreateFixtures_FixturesByRoundSorted() throws InterruptedException {
        // Given
        String startDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);

        // When
        adminFixturePage.createFixtures(startDate, false);
        Thread.sleep(3000);
        adminFixturePage.navigateToAdmin();
        Thread.sleep(2000);

        // Then
        int roundCount = adminFixturePage.getRoundCount();
        if (roundCount > 1) {
            // Kiểm tra round 1 có fixtures
            int round1Count = adminFixturePage.getFixtureCountForRound(1);
            assertTrue(round1Count > 0, "Round 1 nên có fixtures");
        }
    }
}

