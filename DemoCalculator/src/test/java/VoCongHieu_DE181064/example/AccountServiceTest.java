package VoCongHieu_DE181064.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceTest {

    private final AccountService service = new AccountService();

    @DisplayName("Kiểm tra chức năng đăng ký tài khoản")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testRegisterAccount(String username, String password, String email, boolean expected) {
        boolean result = service.registerAccount(username, password, email);
        assertEquals(expected, result, "Sai kết quả cho input: username = " + username
                                                + ", password = " + password
                                                + ", email = " + email);
    }

    @DisplayName("Kiểm tra username có trống không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testUsernameNotEmpty(String username, String password, String email, boolean expected) {
        boolean validUsername = service.isUsernameNotEmpty(username);
        if (!validUsername) {
            assertEquals(expected, validUsername, "Username không được để trống!");
        }
    }

    @DisplayName("Kiểm tra username có từ 3 kí tự trở lên không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testUsernameLengthValid(String username, String password, String email, boolean expected) {
        boolean validUsername = service.isUsernameLengthValid(username);
        if (!validUsername) {
            assertEquals(expected, validUsername, "Username phải từ 3 kí tự trở lên: " + username);
        }
    }

    @DisplayName("Kiểm tra username có hợp lệ không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testUsernameValidation(String username, String password, String email, boolean expected) {
        boolean validUsername = service.isValidUsername(username);
        if (!validUsername) {
            assertEquals(expected, validUsername, "Username không hợp lệ: " + username);
        }
    }

    @DisplayName("Kiểm tra password có trống không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testPasswordNotEmpty(String username, String password, String email, boolean expected) {
        boolean validPassword = service.isPasswordNotEmpty(password);
        if (!validPassword) {
            assertEquals(expected, validPassword, "Password không được để trống!");
        }
    }

    @DisplayName("Kiểm tra password có từ 6 kí tự trở lên không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testPasswordLengthValid(String username, String password, String email, boolean expected) {
        boolean validPassword = service.isPasswordLengthValid(password);
        if (!validPassword) {
            assertEquals(expected, validPassword, "Password phải từ 6 kí tự trở lên: " + password);
        }
    }

    @DisplayName("Kiểm tra password có hợp lệ không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testPasswordValidation(String username, String password, String email, boolean expected) {
        boolean validPassword = service.isValidPassword(password);
        if (!validPassword) {
            assertEquals(expected, validPassword, "Password yếu: " + password);
        }
    }

    @DisplayName("Kiểm tra email có trống không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testEmailNotEmpty(String username, String password, String email, boolean expected) {
        boolean validEmail = service.isEmailNotEmpty(email);
        if (!validEmail) {
            assertEquals(expected, validEmail, "Email không được để trống!");
        }
    }

    @DisplayName("Kiểm tra email có hợp lệ không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testEmailValidation(String username, String password, String email, boolean expected) {
        boolean validEmail = service.isValidEmail(email);
        if (!validEmail) {
            assertEquals(expected, validEmail, "Email không hợp lệ: " + email);
        }
    }

    @DisplayName("Kiểm tra email có trùng không")
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    void testDuplicateEmail(String username, String password, String email, boolean expected) {
        InputStream is = getClass().getResourceAsStream("/test-data.csv");
        assertNotNull(is, "Không tìm thấy file test-data.csv trong resources!");

        List<String> emails = new BufferedReader(new InputStreamReader(is))
                .lines()
                .skip(1)
                .map(line -> line.split(",")[2].trim())
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toList());

        boolean result = service.isDuplicateEmail(email, emails);
        if (!result) {
            assertEquals(expected, result, "Email đã được sử dụng: " + email);
        }
    }
}
