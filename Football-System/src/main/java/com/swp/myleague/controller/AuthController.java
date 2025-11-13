package com.swp.myleague.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.Role;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.repo.UserRepo;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.payload.request.SignupRequest;
import com.swp.myleague.security.jwt.JwtUtils;
import com.swp.myleague.security.service.UserDetailsImpl;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepo userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "LoginPage"; // Thymeleaf template: LoginPage.html
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {
        try {
            // Kiểm tra user có tồn tại không
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                model.addAttribute("error", "Mật khẩu không đúng hoặc tài khoản không tồn tại");
                return "LoginPage";
            }
            
            if (user.getIsBan()) {
                model.addAttribute("error", "❌ Tài khoản của bạn đã bị khóa");
                return "LoginPage";
            }
            
            // Thử authenticate (có thể fail nếu mật khẩu sai)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

            // Set cookie vào response nếu bạn dùng Spring Security mặc định thì không cần
            // thủ công
            model.addAttribute("message", "✅ Đăng nhập thành công");
            model.addAttribute("username", userDetails.getUsername());

            response.addHeader("Set-Cookie", jwtCookie.toString());
            return "redirect:/home"; // hoặc return trang chính sau khi đăng nhập
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Mật khẩu sai
            model.addAttribute("error", "Mật khẩu không đúng hoặc tài khoản không tồn tại");
            return "LoginPage";
        } catch (Exception e) {
            System.out.println(e);
            model.addAttribute("error", "Mật khẩu không đúng hoặc tài khoản không tồn tại");
            return "LoginPage";
        }
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "SignupPage"; // tạo SignupPage.html
    }

    @PostMapping("/signup")
    public String registerUser(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        // Validate email format
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailRegex);
        if (email == null || email.trim().isEmpty() || !pattern.matcher(email).matches()) {
            model.addAttribute("error", "vui lòng nhập đúng định dạng");
            return "SignupPage";
        }
        
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "❌ Username đã được sử dụng");
            return "SignupPage";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "❌ Email đã được sử dụng");
            return "SignupPage";
        }

        SignupRequest signup = new SignupRequest();
        signup.setEmail(email);
        signup.setPassword(password);
        signup.setRole("USER");
        signup.setUsername(username);
        String token = jwtUtils.generateVerificationToken(signup);
        String verifyUrl = "http://localhost:8080/auth/verify?token=" + token;

        emailService.sendMail("chumlu2102@gmail.com", email, "Xác thực tài khoản", verifyUrl, null);

        model.addAttribute("message", "✅ Vui lòng kiểm tra email để xác thực tài khoản");
        return "SignupPage";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        SignupRequest signup = jwtUtils.parseVerificationToken(token);
        if (signup == null) {
            model.addAttribute("error", "❌ Link xác thực không hợp lệ hoặc đã hết hạn");
            return "LoginPage";
        }

        User user = new User(signup.getUsername(), signup.getEmail(), encoder.encode(signup.getPassword()));
        user.setIsBan(false);
        switch (signup.getRole()) {
            case "admin":
                user.setRole(Role.ADMIN);
                break;
            case "club_manager":
                user.setRole(Role.CLUB_MANAGER);
                break;
            case "referee":
                user.setRole(Role.REFEREE);
                break;
            default:
                user.setRole(Role.USER);
        }
        userRepository.save(user);

        model.addAttribute("message", "✅ Đăng ký thành công! Bạn có thể đăng nhập.");
        return "LoginPage";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, Model model) {
        ResponseCookie deleteCookie = ResponseCookie.from("myleague", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());
        SecurityContextHolder.clearContext();
        return "redirect:/auth/login";
    }

    // @PostMapping("/signin")
    // public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest
    // loginRequest) {

    // Authentication authentication = authenticationManager
    // .authenticate(new
    // UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
    // loginRequest.getPassword()));

    // SecurityContextHolder.getContext().setAuthentication(authentication);

    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // authentication.getPrincipal();

    // ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    // List<String> roles = userDetails.getAuthorities().stream()
    // .map(item -> item.getAuthority())
    // .collect(Collectors.toList());

    // return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
    // jwtCookie.toString())
    // .body(new UserInfoResponse(userDetails.getId(),
    // userDetails.getUsername(),
    // userDetails.getEmail(),
    // roles));
    // }

    // @PostMapping("/signup")
    // public ResponseEntity<?> registerUser(@Validated @RequestBody SignupRequest
    // signUpRequest) {
    // if (userRepository.existsByUsername(signUpRequest.getUsername())) {
    // return ResponseEntity.badRequest().body(new MessageResponse("Error: Username
    // is already taken!"));
    // }

    // if (userRepository.existsByEmail(signUpRequest.getEmail())) {
    // return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is
    // already in use!"));
    // }

    // // ✅ Tạo token xác thực (dùng JWT hoặc UUID)
    // String token = jwtUtils.generateVerificationToken(signUpRequest); // hàm
    // custom
    // String verificationUrl = "http://localhost:8080/auth/verify?token=" + token;

    // // ✅ Gửi mail xác thực
    // emailService.sendMail("chumlu2102@gmail.com", signUpRequest.getEmail(),
    // "VERIFY", verificationUrl);

    // return ResponseEntity.ok(new MessageResponse("Verification email sent!"));
    // }

    // @GetMapping("/verify")
    // public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
    // SignupRequest signupData = jwtUtils.parseVerificationToken(token);
    // if (signupData == null) {
    // return ResponseEntity.badRequest().body("❌ Invalid or expired token");
    // }

    // // ✅ Nếu hợp lệ → lưu user vào DB
    // User user = new User(signupData.getUsername(), signupData.getEmail(),
    // encoder.encode(signupData.getPassword()));
    // Role role = Role.USER;
    // switch (signupData.getRole()) {
    // case "admin":
    // role = Role.ADMIN;
    // break;
    // case "club_manager":
    // role = Role.CLUB_MANAGER;
    // break;
    // case "referee":
    // role = Role.REFEREE;
    // break;
    // }
    // user.setRole(role);
    // userRepository.save(user);

    // return ResponseEntity.ok(new MessageResponse("✅ Email verified! You can now
    // sign in."));
    // }

    // @PostMapping("/signout")
    // public ResponseEntity<?> logoutUser() {
    // ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    // return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
    // .body(new MessageResponse("You've been signed out!"));
    // }

    // @PostMapping("/forgot-password")
    // public ResponseEntity<?> forgotPassword(@RequestParam("email") String email)
    // {
    // User user = userRepository.findByEmail(email).orElse(null);
    // if (user == null) {
    // return ResponseEntity.badRequest().body(new MessageResponse("❌ Email không
    // tồn tại trong hệ thống"));
    // }

    // String token = jwtUtils.generateResetPasswordToken(user); // hàm custom, tạo
    // token chứa userId + thời hạn
    // String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;

    // emailService.sendMail("chumlu2102@gmail.com", email, "RESET PASSWORD",
    // resetUrl);

    // return ResponseEntity.ok(new MessageResponse("✅ Đã gửi liên kết đặt lại mật
    // khẩu đến email của bạn"));
    // }

    // @PutMapping("/reset-password")
    // public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
    // @RequestParam("newPassword") String newPassword) {
    // UUID userId = jwtUtils.parseResetPasswordToken(token); // Trả về UUID thay vì
    // Long
    // if (userId == null) {
    // return ResponseEntity.badRequest().body(new MessageResponse("❌ Token không
    // hợp lệ hoặc đã hết hạn"));
    // }

    // User user = userRepository.findById(userId).orElse(null);
    // if (user == null) {
    // return ResponseEntity.badRequest().body(new MessageResponse("❌ Không tìm thấy
    // người dùng"));
    // }

    // user.setPassword(encoder.encode(newPassword));
    // userRepository.save(user);

    // return ResponseEntity.ok(new MessageResponse("✅ Mật khẩu đã được đặt lại
    // thành công"));
    // }

    @GetMapping("/forgot-password-form")
    public String showForgotPasswordForm() {
        return "forgot-password-form"; // Thymeleaf template: resources/templates/forgot-password-form.html
    }

    @PostMapping("/forgot-password-form")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "❌ Email không tồn tại trong hệ thống");
            return "forgot-password-form";
        }

        String token = jwtUtils.generateResetPasswordToken(user);
        String resetUrl = "http://localhost:8080/auth/reset-password-form?token=" + token;

        emailService.sendMail("chumlu2102@gmail.com", email, "RESET PASSWORD", resetUrl, null);

        model.addAttribute("message", "✅ Vui lòng kiểm tra email để đặt lại mật khẩu");
        return "forgot-password-form";
    }

    @GetMapping("/reset-password-form")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        UUID userId = jwtUtils.parseResetPasswordToken(token);
        if (userId == null) {
            model.addAttribute("error", "❌ Token không hợp lệ hoặc đã hết hạn");
            return "reset-password-form";
        }

        model.addAttribute("token", token);
        return "reset-password-form"; // resources/templates/reset-password-form.html
    }

    @PostMapping("/reset-password-form")
    public String handleResetPassword(@RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            Model model) {
        UUID userId = jwtUtils.parseResetPasswordToken(token);
        if (userId == null) {
            model.addAttribute("error", "❌ Token không hợp lệ hoặc đã hết hạn");
            return "reset-password-form";
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            model.addAttribute("error", "❌ Không tìm thấy người dùng");
            return "reset-password-form";
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("message", "✅ Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập.");
        return "redirect:/auth/login"; // Chuyển hướng đến trang đăng nhập
    }

}
