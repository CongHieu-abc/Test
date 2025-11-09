package tests;

import org.junit.jupiter.api.*;
import pages.RegistrationPage;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegistrationTest extends BaseTest {

    private RegistrationPage regPage;

    @BeforeAll
    void setupPage() {
        regPage = new RegistrationPage(driver);
    }

    // CASE 1: Điền hợp lệ - Submit thành công
    @Test
    @DisplayName("TC01 - Đăng ký thành công với dữ liệu hợp lệ")
    void testRegisterSuccess() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an@gmail.com", "0123456789");
        regPage.chooseGender("Male");
        regPage.pickDateOfBirth("10 Oct 2000");
        regPage.enterSubject("Maths");
        regPage.selectHobbyMusic();
        regPage.uploadPicture("avatar.jpg");
        regPage.fillAddress("Ha Noi");
        regPage.selectStateAndCity();
        regPage.submit();

        assertTrue(regPage.isModalVisible(), "Modal không hiện sau khi submit!");
        assertTrue(regPage.getModalContent().contains("An Nguyen"), "Không thấy tên trong modal!");
        regPage.closeModal();
    }

    // CASE 2: Thiếu dữ liệu bắt buộc
    @Test
    @DisplayName("TC02 - Thiếu First Name thì không submit được")
    void testMissingFirstName() {
        regPage.open();
        regPage.fillBasicInfo("", "Nguyen", "an@gmail.com", "0123456789");
        regPage.chooseGender("Male");
        regPage.submit();
        assertFalse(regPage.isModalVisible(), "Modal không nên hiển thị khi thiếu dữ liệu!");
    }

    // CASE 3: Sai định dạng email
    @Test
    @DisplayName("TC03 - Email sai định dạng")
    void testInvalidEmail() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an.gmail.com", "0123456789");
        regPage.chooseGender("Male");
        regPage.submit();
        assertFalse(regPage.isModalVisible(), "Form vẫn submit dù email sai định dạng!");
    }

    // CASE 4: Thiếu Gender
    @Test
    @DisplayName("TC04 - Không chọn giới tính thì không submit được")
    void testMissingGender() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an@gmail.com", "0123456789");
        regPage.submit();
        assertFalse(regPage.isModalVisible(), "Form không nên submit khi thiếu Gender!");
    }

    // CASE 5: Thiếu Mobile
    @Test
    @DisplayName("TC05 - Thiếu số điện thoại thì không submit được")
    void testMissingMobile() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an@gmail.com", "");
        regPage.chooseGender("Male");
        regPage.submit();
        assertFalse(regPage.isModalVisible(), "Form không nên submit khi thiếu số điện thoại!");
    }

    // CASE 6: Sai định dạng Mobile (ít hơn 10 số)
    @Test
    @DisplayName("TC06 - Số điện thoại không đủ 10 chữ số")
    void testInvalidMobile() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an@gmail.com", "12345");
        regPage.chooseGender("Male");
        regPage.submit();
        assertFalse(regPage.isModalVisible(), "Form không nên submit khi số điện thoại sai định dạng!");
    }

    // CASE 7: Thiếu địa chỉ
    @Test
    @DisplayName("TC07 - Thiếu địa chỉ thì không submit được")
    void testMissingAddress() {
        regPage.open();
        regPage.fillBasicInfo("An", "Nguyen", "an@gmail.com", "0123456789");
        regPage.chooseGender("Male");
        regPage.enterSubject("English");
        regPage.selectHobbyMusic();
        regPage.selectStateAndCity();
        regPage.submit();
        assertTrue(regPage.isModalVisible(), "Form vẫn nên submit khi thiếu địa chỉ (DemoQA không required).");
    }

    // CASE 8: Upload ảnh thành công
    @Test
    @DisplayName("TC08 - Upload ảnh thành công và hiển thị trong modal")
    void testUploadPictureSuccess() {
        regPage.open();
        regPage.fillBasicInfo("Binh", "Tran", "binh@gmail.com", "0987654321");
        regPage.chooseGender("Male");
        regPage.enterSubject("Physics");
        regPage.selectHobbyMusic();
        regPage.uploadPicture("avatar.jpg");
        regPage.fillAddress("Ho Chi Minh");
        regPage.selectStateAndCity();
        regPage.submit();

        assertTrue(regPage.isModalVisible(), "Modal không hiển thị!");
        assertTrue(regPage.getModalContent().contains("avatar.jpg"), "Ảnh upload không hiển thị trong modal!");
        regPage.closeModal();
    }
}
