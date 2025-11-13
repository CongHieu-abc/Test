package com.swp.myleague.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SignupPage extends BasePage {
    
    // Locators
    private static final By USERNAME_INPUT = By.name("username");
    private static final By EMAIL_INPUT = By.name("email");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By SIGNUP_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGE = By.cssSelector("p.text-danger, .text-danger, .alert-danger");
    private static final By SUCCESS_MESSAGE = By.cssSelector("p.text-success, .text-success, .alert-success");
    
    public SignupPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToSignup() {
        navigateTo("http://localhost:8080/auth/signup");
        // Disable HTML5 validation để Selenium có thể test các trường hợp invalid
        disableHTML5Validation();
    }
    
    private void disableHTML5Validation() {
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript(
                "var form = document.getElementById('signupForm');" +
                "if (form) { form.setAttribute('novalidate', 'novalidate'); }"
            );
        }
    }
    
    public void enterUsername(String username) {
        type(USERNAME_INPUT, username);
    }
    
    public void enterEmail(String email) {
        type(EMAIL_INPUT, email);
    }
    
    public void enterPassword(String password) {
        type(PASSWORD_INPUT, password);
    }
    
    public void clickSignup() {
        click(SIGNUP_BUTTON);
    }
    
    public void signup(String username, String email, String password) {
        // Đảm bảo HTML5 validation đã được disable
        disableHTML5Validation();
        enterUsername(username);
        enterEmail(email);
        enterPassword(password);
        clickSignup();
    }
    
    public void signupWithDisabledClientValidation(String username, String email, String password) {
        // Disable both HTML5 and client-side validation
        disableHTML5Validation();
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver).executeScript(
                "var form = document.getElementById('signupForm');" +
                "if (form) { form.onsubmit = null; }"
            );
        }
        enterUsername(username);
        enterEmail(email);
        enterPassword(password);
        clickSignup();
    }
    
    public String getErrorMessage() {
        try {
            // First check for alert dialog
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String alertText = alert.getText();
                alert.accept();
                if (alertText.contains("vui lòng nhập đúng định dạng")) {
                    return alertText;
                }
            } catch (Exception e) {
                // No alert, continue to check page element
            }
            
            // Check for error message on page
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
            // Check for alert first (with wait)
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String alertText = alert.getText();
                if (alertText != null && 
                    (alertText.contains("vui lòng nhập đúng định dạng") || 
                     alertText.contains("vui lòng nhập đúng định dạng email"))) {
                    alert.accept();
                    return true;
                }
                alert.accept();
            } catch (Exception e) {
                // No alert, continue to check page element
            }
            
            // Check for error message on page
            try {
                WebElement errorElement = waitForVisibility(ERROR_MESSAGE);
                String errorText = errorElement.getText();
                return errorElement.isDisplayed() && 
                       errorText != null && 
                       !errorText.trim().isEmpty() &&
                       (errorText.contains("vui lòng nhập đúng định dạng") || 
                        errorText.contains("Username đã được sử dụng") ||
                        errorText.contains("Email đã được sử dụng"));
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement successElement = driver.findElement(SUCCESS_MESSAGE);
            return successElement.isDisplayed() && !successElement.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasAlertWithError() {
        try {
            // Wait for alert to appear (with timeout)
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            String alertText = alert.getText();
            boolean hasError = alertText != null && 
                              (alertText.contains("vui lòng nhập đúng định dạng") || 
                               alertText.contains("vui lòng nhập đúng định dạng email"));
            alert.accept();
            return hasError;
        } catch (Exception e) {
            // Try to check if alert exists without waiting
            try {
                Alert alert = driver.switchTo().alert();
                String alertText = alert.getText();
                boolean hasError = alertText != null && 
                                  (alertText.contains("vui lòng nhập đúng định dạng") || 
                                   alertText.contains("vui lòng nhập đúng định dạng email"));
                alert.accept();
                return hasError;
            } catch (Exception e2) {
                return false;
            }
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

