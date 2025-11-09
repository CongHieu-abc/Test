package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.time.Duration;

public class RegistrationPage extends BasePage {

    // ===== LOCATORS =====
    private final String URL = "https://demoqa.com/automation-practice-form";

    private By firstName = By.id("firstName");
    private By lastName = By.id("lastName");
    private By email = By.id("userEmail");
    private By genderMale = By.xpath("//label[text()='Male']");
    private By genderFemale = By.xpath("//label[text()='Female']");
    private By mobile = By.id("userNumber");
    private By dobInput = By.id("dateOfBirthInput");
    private By subjectInput = By.id("subjectsInput");
    private By hobbyMusic = By.xpath("//label[text()='Music']");
    private By uploadPicture = By.id("uploadPicture");
    private By address = By.id("currentAddress");
    private By stateDropdown = By.id("state");
    private By stateOption = By.xpath("//div[text()='NCR']");
    private By cityDropdown = By.id("city");
    private By cityOption = By.xpath("//div[text()='Delhi']");
    private By submitBtn = By.id("submit");
    private By modalTitle = By.id("example-modal-sizes-title-lg");
    private By modalBody = By.xpath("//div[@class='modal-body']");
    private By closeModalBtn = By.id("closeLargeModal");

    // ===== CONSTRUCTOR =====
    public RegistrationPage(WebDriver driver) {
        super(driver);
    }

    // ===== ACTIONS =====
    public void open() {
        navigateTo(URL);
    }

    public void fillBasicInfo(String fName, String lName, String mail, String phone) {
        type(firstName, fName);
        type(lastName, lName);
        type(email, mail);
        type(mobile, phone);
    }

    public void chooseGender(String gender) {
        if (gender.equalsIgnoreCase("male")) click(genderMale);
        else click(genderFemale);
    }

    public void pickDateOfBirth(String date) {
        click(dobInput);
        WebElement datePicker = driver.findElement(By.className("react-datepicker__month-container"));
        // Bạn có thể viết logic chọn ngày chi tiết, ở đây demo chọn 10/10/2000:
        driver.findElement(By.cssSelector(".react-datepicker__day--010")).click();
    }

    public void enterSubject(String subject) {
        type(subjectInput, subject);
        driver.findElement(subjectInput).sendKeys(Keys.ENTER);
    }

    public void selectHobbyMusic() {
        click(hobbyMusic);
    }

    public void uploadPicture(String fileName) {
        String fullPath = Paths.get("src/test/resources/images", fileName).toAbsolutePath().toString();
        driver.findElement(uploadPicture).sendKeys(fullPath);
    }

    public void fillAddress(String addr) {
        type(address, addr);
    }

    public void selectStateAndCity() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Cuộn xuống để chắc chắn nhìn thấy dropdown
        js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(stateDropdown));

        // Mở dropdown State
        WebElement stateDrop = wait.until(ExpectedConditions.elementToBeClickable(stateDropdown));
        stateDrop.click();

        // Chờ danh sách xuất hiện và chọn NCR (JS click để tránh bị chặn)
        WebElement stateOpt = wait.until(ExpectedConditions.presenceOfElementLocated(stateOption));
        js.executeScript("arguments[0].click();", stateOpt);

        // Mở dropdown City
        WebElement cityDrop = wait.until(ExpectedConditions.elementToBeClickable(cityDropdown));
        cityDrop.click();

        // Chờ danh sách City load, pause nhỏ tránh animation
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement cityOpt = shortWait.until(ExpectedConditions.presenceOfElementLocated(cityOption));

        // Dùng JavaScript click thay vì click() để bỏ qua overlay
        js.executeScript("arguments[0].click();", cityOpt);
    }

    public void submit() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(submitBtn));
        click(submitBtn);
    }

    public boolean isModalVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalTitle));
            return driver.findElement(modalTitle).isDisplayed();
        } catch (TimeoutException e) {
            // Nếu không thấy modal trong thời gian chờ => coi như "không hiển thị"
            return false;
        }
    }

    public String getModalContent() {
        return getText(modalBody);
    }

    public void closeModal() {
        click(closeModalBtn);
    }
}
