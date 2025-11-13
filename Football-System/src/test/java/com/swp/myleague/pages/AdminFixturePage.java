package com.swp.myleague.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AdminFixturePage extends BasePage {
    
    // Locators - Updated based on actual HTML structure
    private static final By GENERATE_FIXTURES_TAB = By.cssSelector("a[onclick*=\"showTab('generate-fixtures')\"]");
    private static final By ROUND_HEADINGS = By.cssSelector("h5.text-primary");
    private static final By FIXTURE_CARDS = By.cssSelector("#generate-fixtures .card");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger");
    private static final By SAVE_ROUND_BUTTONS = By.cssSelector("form[action*='save-round'] button[type='submit']");
    
    public AdminFixturePage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToAdmin() {
        navigateTo("http://localhost:8080/admin");
    }
    
    public void openGenerateFixturesTab() {
        try {
            click(GENERATE_FIXTURES_TAB);
            // Wait a bit for tab to switch
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            System.out.println("Could not click generate fixtures tab: " + e.getMessage());
        }
    }
    
    public void createFixtures(String startDate, boolean recreate) {
        // Use direct URL approach - simpler and more reliable
        // Since we're already logged in, we can directly navigate to the create endpoint
        // Always include recreate parameter to be explicit
        String url = "http://localhost:8080/admin/fixture/create?startDate=" + startDate 
                   + "&recreate=" + recreate;
        navigateTo(url);
    }
    
    public void saveRound(int roundNumber) {
        try {
            // Open generate fixtures tab first
            openGenerateFixturesTab();
            
            // Find the save button for the specific round
            List<WebElement> saveButtons = driver.findElements(SAVE_ROUND_BUTTONS);
            for (WebElement button : saveButtons) {
                // Find the form that contains this button
                WebElement form = button.findElement(By.xpath("./ancestor::form"));
                // Check if this form has the correct roundNumber in hidden input
                try {
                    WebElement roundInput = form.findElement(By.name("roundNumber"));
                    String roundValue = roundInput.getAttribute("value");
                    if (roundValue != null && roundValue.equals(String.valueOf(roundNumber))) {
                        button.click();
                        return;
                    }
                } catch (Exception e) {
                    // If hidden input not found, check button text
                    String buttonText = button.getText();
                    if (buttonText.contains("Vòng " + roundNumber) || buttonText.contains("Round " + roundNumber)) {
                        button.click();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not find save button for round " + roundNumber + ": " + e.getMessage());
            // Note: Cannot use GET for POST endpoint, so we'll let the test fail
            // or we could use JavaScript to submit the form
        }
    }
    
    public int getRoundCount() {
        try {
            // Open generate fixtures tab first
            openGenerateFixturesTab();
            List<WebElement> rounds = driver.findElements(ROUND_HEADINGS);
            return rounds.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getFixtureCount() {
        try {
            // Open generate fixtures tab first
            openGenerateFixturesTab();
            List<WebElement> fixtures = driver.findElements(FIXTURE_CARDS);
            return fixtures.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getFixtureCountForRound(int roundNumber) {
        try {
            openGenerateFixturesTab();
            // Find the round heading
            List<WebElement> roundHeadings = driver.findElements(ROUND_HEADINGS);
            for (WebElement heading : roundHeadings) {
                if (heading.getText().contains("Vòng " + roundNumber)) {
                    // Find parent container and count cards within it
                    WebElement roundSection = heading.findElement(By.xpath("./ancestor::div[contains(@class, 'mb-4')]"));
                    List<WebElement> cards = roundSection.findElements(By.cssSelector(".card"));
                    return cards.size();
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean isFixtureTableDisplayed() {
        try {
            openGenerateFixturesTab();
            // Check if there are any fixture cards or round headings
            List<WebElement> rounds = driver.findElements(ROUND_HEADINGS);
            List<WebElement> cards = driver.findElements(FIXTURE_CARDS);
            return !rounds.isEmpty() || !cards.isEmpty();
        } catch (Exception e) {
            return false;
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
    
    public String getErrorMessage() {
        try {
            WebElement errorElement = waitForVisibility(ERROR_MESSAGE);
            return errorElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean hasFixturesInSession() {
        // Check if fixtures are displayed (indicating session has fixtures)
        // Also check for progress message or round headings
        try {
            openGenerateFixturesTab();
            // Check for progress alert or round headings
            List<WebElement> progressAlerts = driver.findElements(By.cssSelector(".alert-info"));
            List<WebElement> rounds = driver.findElements(ROUND_HEADINGS);
            List<WebElement> cards = driver.findElements(FIXTURE_CARDS);
            
            // Has fixtures if there are rounds or cards, or progress message indicates fixtures
            boolean hasProgressMessage = !progressAlerts.isEmpty();
            boolean hasRounds = !rounds.isEmpty();
            boolean hasCards = !cards.isEmpty();
            
            return hasProgressMessage || hasRounds || hasCards;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

