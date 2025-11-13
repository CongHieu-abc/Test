package com.swp.myleague.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.swp.myleague.payload.request.SignupRequest;

// Simple fake classes to avoid Mockito inline mocking issues on newer JDKs
class FakeUserRepo implements com.swp.myleague.model.repo.UserRepo {
    private boolean usernameExists = false;
    private boolean emailExists = false;

    void setUsernameExists(boolean exists) {
        this.usernameExists = exists;
    }

    void setEmailExists(boolean exists) {
        this.emailExists = exists;
    }

    @Override
    public boolean existsByUsername(String username) {
        return usernameExists;
    }

    @Override
    public boolean existsByEmail(String email) {
        return emailExists;
    }

    // Implement other required methods from JpaRepository with default values
    @Override
    public java.util.Optional<com.swp.myleague.model.entities.User> findByUsername(String username) {
        return java.util.Optional.empty();
    }

    @Override
    public java.util.Optional<com.swp.myleague.model.entities.User> findByEmail(String email) {
        return java.util.Optional.empty();
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> java.util.List<S> saveAll(
            java.lang.Iterable<S> entities) {
        return java.util.Collections.emptyList();
    }

    @Override
    public java.util.Optional<com.swp.myleague.model.entities.User> findById(java.util.UUID id) {
        return java.util.Optional.empty();
    }

    @Override
    public boolean existsById(java.util.UUID id) {
        return false;
    }

    @Override
    public java.util.List<com.swp.myleague.model.entities.User> findAll() {
        return java.util.Collections.emptyList();
    }

    @Override
    public java.util.List<com.swp.myleague.model.entities.User> findAllById(
            java.lang.Iterable<java.util.UUID> ids) {
        return java.util.Collections.emptyList();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(java.util.UUID id) {
    }

    @Override
    public void delete(com.swp.myleague.model.entities.User entity) {
    }

    @Override
    public void deleteAllById(java.lang.Iterable<? extends java.util.UUID> ids) {
    }

    @Override
    public void deleteAll(java.lang.Iterable<? extends com.swp.myleague.model.entities.User> entities) {
    }

    @Override
    public void deleteAll() {
    }

    // Note: Specification methods are from JpaSpecificationExecutor, not directly in JpaRepository
    // These are not required for basic JpaRepository implementation

    @Override
    public void flush() {
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> S saveAndFlush(S entity) {
        return entity;
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> java.util.List<S> saveAllAndFlush(
            java.lang.Iterable<S> entities) {
        return java.util.Collections.emptyList();
    }

    @Override
    public void deleteAllInBatch(java.lang.Iterable<com.swp.myleague.model.entities.User> entities) {
    }

    @Override
    public void deleteAllByIdInBatch(java.lang.Iterable<java.util.UUID> ids) {
    }

    @Override
    public void deleteAllInBatch() {
    }

    @Override
    @Deprecated
    public com.swp.myleague.model.entities.User getOne(java.util.UUID id) {
        return null;
    }

    @Override
    @Deprecated
    public com.swp.myleague.model.entities.User getById(java.util.UUID id) {
        return null;
    }
    
    @Override
    public <S extends com.swp.myleague.model.entities.User> java.util.Optional<S> findOne(
            org.springframework.data.domain.Example<S> example) {
        return java.util.Optional.empty();
    }
    
    @Override
    public <S extends com.swp.myleague.model.entities.User, R> R findBy(
            org.springframework.data.domain.Example<S> example,
            java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public com.swp.myleague.model.entities.User getReferenceById(java.util.UUID id) {
        return null;
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> java.util.List<S> findAll(
            org.springframework.data.domain.Example<S> example) {
        return java.util.Collections.emptyList();
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> java.util.List<S> findAll(
            org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
        return java.util.Collections.emptyList();
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> org.springframework.data.domain.Page<S> findAll(
            org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
        return org.springframework.data.domain.Page.empty();
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> long count(
            org.springframework.data.domain.Example<S> example) {
        return 0;
    }

    @Override
    public <S extends com.swp.myleague.model.entities.User> boolean exists(
            org.springframework.data.domain.Example<S> example) {
        return false;
    }

    @Override
    public java.util.List<com.swp.myleague.model.entities.User> findAll(org.springframework.data.domain.Sort sort) {
        return java.util.Collections.emptyList();
    }

    @Override
    public org.springframework.data.domain.Page<com.swp.myleague.model.entities.User> findAll(
            org.springframework.data.domain.Pageable pageable) {
        return org.springframework.data.domain.Page.empty();
    }
}

class FakeJwtUtils extends com.swp.myleague.security.jwt.JwtUtils {
    private String lastToken = "test-token-123";

    @Override
    public String generateVerificationToken(SignupRequest signup) {
        return lastToken;
    }

    String getLastToken() {
        return lastToken;
    }
}

class FakeEmailService extends com.swp.myleague.model.service.EmailService {
    private String lastToEmail;
    private String lastSubject;
    private String lastContent;

    public FakeEmailService() {
        super(null); // Pass null for JavaMailSender since we're not actually sending emails
    }

    @Override
    public void sendMail(String from, String to, String subject, String text, byte[] imageBytes) {
        this.lastToEmail = to;
        this.lastSubject = subject;
        this.lastContent = text;
    }

    String getLastToEmail() {
        return lastToEmail;
    }

    String getLastSubject() {
        return lastSubject;
    }

    String getLastContent() {
        return lastContent;
    }
}

public class AuthControllerTest {

    private AuthController controller;
    private FakeUserRepo userRepo;
    private FakeJwtUtils jwtUtils;
    private FakeEmailService emailService;

    @BeforeEach
    void setup() {
        controller = new AuthController();
        userRepo = new FakeUserRepo();
        jwtUtils = new FakeJwtUtils();
        emailService = new FakeEmailService();

        // Inject fakes into controller
        ReflectionTestUtils.setField(controller, "userRepository", userRepo);
        ReflectionTestUtils.setField(controller, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(controller, "emailService", emailService);
    }

    // ========== TEST CASE 1: Email null/empty/whitespace ==========
    @Test
    @DisplayName("TC1: Email null/empty/whitespace - trả về error 'vui lòng nhập đúng định dạng'")
    void testRegisterUser_EmailNullEmptyWhitespace_ReturnsError() {
        Model model1 = new ExtendedModelMap();
        String result1 = controller.registerUser("testuser", null, "password123", model1);
        assertEquals("SignupPage", result1);
        assertEquals("vui lòng nhập đúng định dạng", model1.getAttribute("error"));
        
        Model model2 = new ExtendedModelMap();
        String result2 = controller.registerUser("testuser", "   ", "password123", model2);
        assertEquals("SignupPage", result2);
        assertEquals("vui lòng nhập đúng định dạng", model2.getAttribute("error"));
        
        Model model3 = new ExtendedModelMap();
        String result3 = controller.registerUser("testuser", "\t\n\r", "password123", model3);
        assertEquals("SignupPage", result3);
        assertEquals("vui lòng nhập đúng định dạng", model3.getAttribute("error"));
    }

    // ========== TEST CASE 2: Email invalid format cơ bản ==========
    @Test
    @DisplayName("TC2: Email invalid format cơ bản - trả về error")
    void testRegisterUser_EmailInvalidBasicFormat_ReturnsError() {
        // Email không có @
        Model model1 = new ExtendedModelMap();
        String result1 = controller.registerUser("testuser", "invalidemail.com", "password123", model1);
        assertEquals("SignupPage", result1);
        assertEquals("vui lòng nhập đúng định dạng", model1.getAttribute("error"));
        
        // Email không có domain
        Model model2 = new ExtendedModelMap();
        String result2 = controller.registerUser("testuser", "user@", "password123", model2);
        assertEquals("SignupPage", result2);
        assertEquals("vui lòng nhập đúng định dạng", model2.getAttribute("error"));
        
        // Email không có TLD
        Model model3 = new ExtendedModelMap();
        String result3 = controller.registerUser("testuser", "user@domain", "password123", model3);
        assertEquals("SignupPage", result3);
        assertEquals("vui lòng nhập đúng định dạng", model3.getAttribute("error"));
        
        // Email có nhiều @
        Model model4 = new ExtendedModelMap();
        String result4 = controller.registerUser("testuser", "user@@example.com", "password123", model4);
        assertEquals("SignupPage", result4);
        assertEquals("vui lòng nhập đúng định dạng", model4.getAttribute("error"));
        
        // Email bắt đầu bằng @
        Model model5 = new ExtendedModelMap();
        String result5 = controller.registerUser("testuser", "@example.com", "password123", model5);
        assertEquals("SignupPage", result5);
        assertEquals("vui lòng nhập đúng định dạng", model5.getAttribute("error"));
        
        // Email kết thúc bằng @
        Model model6 = new ExtendedModelMap();
        String result6 = controller.registerUser("testuser", "user@", "password123", model6);
        assertEquals("SignupPage", result6);
        assertEquals("vui lòng nhập đúng định dạng", model6.getAttribute("error"));
    }

    // ========== TEST CASE 3: Email có ký tự không hợp lệ ==========
    @Test
    @DisplayName("TC3: Email có ký tự không hợp lệ - trả về error")
    void testRegisterUser_EmailInvalidCharacters_ReturnsError() {
        // Email có ký tự đặc biệt không hợp lệ
        Model model1 = new ExtendedModelMap();
        String result1 = controller.registerUser("testuser", "user@#$%@domain.com", "password123", model1);
        assertEquals("SignupPage", result1);
        assertEquals("vui lòng nhập đúng định dạng", model1.getAttribute("error"));
        
        // Email với ký tự Unicode
        Model model2 = new ExtendedModelMap();
        String result2 = controller.registerUser("testuser", "user@exämple.com", "password123", model2);
        assertEquals("SignupPage", result2);
        assertEquals("vui lòng nhập đúng định dạng", model2.getAttribute("error"));
    }

    // ========== TEST CASE 4: Email TLD invalid ==========
    @Test
    @DisplayName("TC4: Email TLD invalid - trả về error")
    void testRegisterUser_EmailInvalidTLD_ReturnsError() {
        // TLD quá dài (>7 ký tự)
        Model model1 = new ExtendedModelMap();
        String result1 = controller.registerUser("newuser", "user@example.toolongtld", "password123", model1);
        assertEquals("SignupPage", result1);
        assertEquals("vui lòng nhập đúng định dạng", model1.getAttribute("error"));
        
        // TLD quá ngắn (<2 ký tự)
        Model model2 = new ExtendedModelMap();
        String result2 = controller.registerUser("newuser", "user@example.c", "password123", model2);
        assertEquals("SignupPage", result2);
        assertEquals("vui lòng nhập đúng định dạng", model2.getAttribute("error"));
    }

    // ========== TEST CASE 5: Đăng ký thành công với email hợp lệ (các format khác nhau) ==========
    @Test
    @DisplayName("TC5: Đăng ký thành công với email hợp lệ (các format khác nhau)")
    void testRegisterUser_ValidEmailFormats_Success() {
        // Email cơ bản
        Model model1 = new ExtendedModelMap();
        String result1 = controller.registerUser("newuser1", "newuser@example.com", "password123", model1);
        assertEquals("SignupPage", result1);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model1.getAttribute("message"));
        assertEquals("newuser@example.com", emailService.getLastToEmail());
        assertEquals("Xác thực tài khoản", emailService.getLastSubject());
        assertTrue(emailService.getLastContent().contains("http://localhost:8080/auth/verify?token="));
        
        // Email với subdomain
        Model model2 = new ExtendedModelMap();
        String result2 = controller.registerUser("newuser2", "user@mail.example.com", "password123", model2);
        assertEquals("SignupPage", result2);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model2.getAttribute("message"));
        
        // Email với dấu + và -
        Model model3 = new ExtendedModelMap();
        String result3 = controller.registerUser("newuser3", "user+tag@example.com", "password123", model3);
        assertEquals("SignupPage", result3);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model3.getAttribute("message"));
        
        // Email với số
        Model model4 = new ExtendedModelMap();
        String result4 = controller.registerUser("newuser4", "user123@example.com", "password123", model4);
        assertEquals("SignupPage", result4);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model4.getAttribute("message"));
        
        // Email với dấu chấm
        Model model5 = new ExtendedModelMap();
        String result5 = controller.registerUser("newuser5", "first.last@example.com", "password123", model5);
        assertEquals("SignupPage", result5);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model5.getAttribute("message"));
        
        // Email với TLD dài (7 ký tự)
        Model model6 = new ExtendedModelMap();
        String result6 = controller.registerUser("newuser6", "user@example.museum", "password123", model6);
        assertEquals("SignupPage", result6);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model6.getAttribute("message"));
        
        // Email với TLD ngắn (2 ký tự)
        Model model7 = new ExtendedModelMap();
        String result7 = controller.registerUser("newuser7", "user@example.co", "password123", model7);
        assertEquals("SignupPage", result7);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model7.getAttribute("message"));
        
        // Email với underscore
        Model model8 = new ExtendedModelMap();
        String result8 = controller.registerUser("newuser8", "user_name@example.com", "password123", model8);
        assertEquals("SignupPage", result8);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model8.getAttribute("message"));
        
        // Email với domain phức tạp
        Model model9 = new ExtendedModelMap();
        String result9 = controller.registerUser("newuser9", "user@mail-server.example.co.uk", "password123", model9);
        assertEquals("SignupPage", result9);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model9.getAttribute("message"));
        
        // Email với dấu * và &
        Model model10 = new ExtendedModelMap();
        String result10 = controller.registerUser("newuser10", "user*test@example.com", "password123", model10);
        assertEquals("SignupPage", result10);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model10.getAttribute("message"));
        
        Model model11 = new ExtendedModelMap();
        String result11 = controller.registerUser("newuser11", "user&test@example.com", "password123", model11);
        assertEquals("SignupPage", result11);
        assertEquals("✅ Vui lòng kiểm tra email để xác thực tài khoản", model11.getAttribute("message"));
    }

    // ========== TEST CASE 6: Email đúng định dạng nhưng username đã tồn tại ==========
    @Test
    @DisplayName("TC6: Email đúng định dạng nhưng username đã tồn tại - trả về error")
    void testRegisterUser_UsernameExists_ReturnsError() {
        userRepo.setUsernameExists(true);
        Model model = new ExtendedModelMap();
        
        String result = controller.registerUser("existinguser", "newuser@example.com", "password123", model);
        
        assertEquals("SignupPage", result);
        assertEquals("❌ Username đã được sử dụng", model.getAttribute("error"));
    }

    // ========== TEST CASE 7: Email đúng định dạng nhưng email đã tồn tại ==========
    @Test
    @DisplayName("TC7: Email đúng định dạng nhưng email đã tồn tại - trả về error")
    void testRegisterUser_EmailExists_ReturnsError() {
        userRepo.setEmailExists(true);
        Model model = new ExtendedModelMap();
        
        String result = controller.registerUser("newuser", "existing@example.com", "password123", model);
        
        assertEquals("SignupPage", result);
        assertEquals("❌ Email đã được sử dụng", model.getAttribute("error"));
    }

    // ========== TEST CASE 8: Username và email đều đã tồn tại (ưu tiên username) ==========
    @Test
    @DisplayName("TC8: Username và email đều đã tồn tại - ưu tiên kiểm tra username trước")
    void testRegisterUser_BothUsernameAndEmailExist_ReturnsUsernameError() {
        userRepo.setUsernameExists(true);
        userRepo.setEmailExists(true);
        Model model = new ExtendedModelMap();
        
        String result = controller.registerUser("existinguser", "existing@example.com", "password123", model);
        
        assertEquals("SignupPage", result);
        assertEquals("❌ Username đã được sử dụng", model.getAttribute("error"));
    }
}
