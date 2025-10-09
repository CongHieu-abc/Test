package VoCongHieu_DE181064.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class AccountServiceTest {

    private final AccountService service = new AccountService();

    @DisplayName("Kiểm tra chức năng đăng ký tài khoản")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testRegisterAccount(String username, String password, String email, boolean expected) {
        boolean result = service.registerAccount(username, password, email);
        assertEquals(expected, result,
                () -> "Sai kết quả cho input: username=" + username
                        + ", password=" + password
                        + ", email=" + email);
    }

    @DisplayName("Kiểm tra username với CSV")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testUsernameValidation(String username, String password, String email, boolean expected) {
        boolean validUsername = service.isValidUsername(username);

        if (username == null || username.isEmpty()) {
            assertFalse(validUsername, "Username không hợp lệ: " + username);
        } else {
            assertTrue(validUsername, "Username hợp lệ nhưng bị từ chối: " + username);
        }
    }

    @DisplayName("Kiểm tra password")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testPasswordValidation(String username, String password, String email, boolean expected) {
        boolean validPassword = service.isValidPassword(password);
        if (password == null || password.length() <= 6) {
            assertFalse(validPassword,
                    () -> "Password phải lớn hơn 6 kí tự: " + password);
        } else {
            assertTrue(validPassword,
                    () -> "Password đúng nhưng bị từ chối: " + password);
        }
    }

    @DisplayName("Kiểm tra email")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testEmailValidation(String username, String password, String email, boolean expected) {
        boolean validEmail = service.isValidEmail(email);
        if (email == null || !email.contains("@")) {
            assertFalse(validEmail,
                    () -> "Email không hợp lệ: " + email);
        } else {
            assertTrue(validEmail,
                    () -> "Email đúng nhưng bị từ chối: " + email);
        }
    }
}
