package com.swp.myleague.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    
    // Locators
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button.btn-login, button[type='submit']");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .error-message");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success, .success-message");
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToLogin() {
        navigateTo("http://localhost:8080/auth/login");
        // Disable HTML5 validation để Selenium có thể test
        disableHTML5Validation();
    }
    
    private void disableHTML5Validation() {
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript(
                "var form = document.querySelector('form[action*=\"login\"]');" +
                "if (form) { form.setAttribute('novalidate', 'novalidate'); }"
            );
        }
    }
    
    public void enterUsername(String username) {
        type(USERNAME_INPUT, username);
    }
    
    public void enterPassword(String password) {
        type(PASSWORD_INPUT, password);
    }
    
    public void clickLogin() {
        click(LOGIN_BUTTON);
    }
    
    public void login(String username, String password) {
        // Đảm bảo HTML5 validation đã được disable
        disableHTML5Validation();
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
    
    public String getErrorMessage() {
        try {
            WebElement errorElement = waitForVisibility(ERROR_MESSAGE);
            return errorElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getSuccessMessage() {
        try {
            WebElement successElement = waitForVisibility(SUCCESS_MESSAGE);
            return successElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            WebElement errorElement = waitForVisibility(ERROR_MESSAGE);
            return errorElement.isDisplayed() && !errorElement.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement successElement = waitForVisibility(SUCCESS_MESSAGE);
            return successElement.isDisplayed() && !successElement.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    public boolean isLoggedIn() {
        // Kiểm tra xem đã redirect về /home sau khi đăng nhập
        try {
            wait.until(ExpectedConditions.urlContains("/home"));
            return driver.getCurrentUrl().contains("/home");
        } catch (Exception e) {
            return false;
        }
    }
}

