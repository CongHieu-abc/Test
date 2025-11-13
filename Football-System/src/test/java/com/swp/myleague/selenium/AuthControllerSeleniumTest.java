package com.swp.myleague.selenium;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import com.swp.myleague.pages.SignupPage;
import com.swp.myleague.utils.DriverFactory;

public class AuthControllerSeleniumTest {

    private WebDriver driver;
    private SignupPage signupPage;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.createDriver();
        signupPage = new SignupPage(driver);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== TEST CASE 1: Đăng ký thành công với email hợp lệ ==========
    @Test
    @DisplayName("TC1: Đăng ký thành công với email hợp lệ")
    void testSignup_ValidEmail_Success() throws InterruptedException {
        // Given
        String username = "testuser_" + System.currentTimeMillis();
        String email = "test_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";

        // When
        signupPage.navigateToSignup();
        signupPage.signup(username, email, password);
        Thread.sleep(2000); // Wait for page to load

        // Then
        assertTrue(signupPage.isSuccessMessageDisplayed() || 
                   signupPage.getSuccessMessage().contains("Vui lòng kiểm tra email"),
                   "Nên hiển thị message thành công");
    }

    // ========== TEST CASE 2: Email invalid format - hiển thị error ==========
    @Test
    @DisplayName("TC2: Email invalid format - hiển thị error")
    void testSignup_InvalidEmailFormat_ShowsError() throws InterruptedException {
        String username = "testuser_" + System.currentTimeMillis();
        String password = "password123";

        // Test empty email
        signupPage.navigateToSignup();
        signupPage.signup(username, "", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho empty email");

        // Test email không có @
        username = "testuser_" + System.currentTimeMillis();
        signupPage.navigateToSignup();
        signupPage.signup(username, "invalidemail.com", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho email không có @");

        // Test email không có domain
        username = "testuser_" + System.currentTimeMillis();
        signupPage.navigateToSignup();
        signupPage.signup(username, "user@", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho email không có domain");

        // Test email không có TLD
        username = "testuser_" + System.currentTimeMillis();
        signupPage.navigateToSignup();
        signupPage.signup(username, "user@domain", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho email không có TLD");

        // Test email có nhiều @
        username = "testuser_" + System.currentTimeMillis();
        signupPage.navigateToSignup();
        signupPage.signup(username, "user@@example.com", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho email có nhiều @");
    }

    // ========== TEST CASE 3: Email TLD invalid - hiển thị error ==========
    @Test
    @DisplayName("TC3: Email TLD invalid - hiển thị error")
    void testSignup_EmailInvalidTLD_ShowsError() throws InterruptedException {
        String username = "testuser_" + System.currentTimeMillis();
        String password = "password123";

        // Test TLD quá dài
        signupPage.navigateToSignup();
        signupPage.signup(username, "user@example.toolongtld", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho TLD quá dài");

        // Test TLD quá ngắn
        username = "testuser_" + System.currentTimeMillis();
        signupPage.navigateToSignup();
        signupPage.signup(username, "user@example.c", password);
        Thread.sleep(2000);
        assertTrue(signupPage.hasAlertWithError() || 
                   signupPage.isErrorMessageDisplayed() || 
                   signupPage.getErrorMessage().contains("vui lòng nhập đúng định dạng"),
                   "Nên hiển thị error message hoặc alert cho TLD quá ngắn");
    }
}

