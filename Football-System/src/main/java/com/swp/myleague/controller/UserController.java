package com.swp.myleague.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.saleproductservice.OrderService;

@Controller
@RequestMapping(value = { "/user", "/user/" })
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    OrderService orderService;

    // Lấy danh sách toàn bộ user (dành cho admin)
    @GetMapping("")
    public String getUser(Model model) {
        List<User> listUsers = userService.getUser();
        model.addAttribute("listUsers", listUsers);
        return "ManagementUser";
    }

    // Lấy user theo ID (cũ, dùng để debug hoặc admin xem)
    @GetMapping("/{userId}")
    public String getUserById(@PathVariable(name = "userId") String userId, Model model) {
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "ManagementUser";
    }

    // Lấy thông tin user hiện tại đang đăng nhập (dựa vào Spring Security)
    @GetMapping("/profile")
    public String getCurrentUserProfile(Model model, Principal principal) {
        String username = principal.getName(); // lấy username từ context
        User user = userService.findByUsername(username); // giả sử bạn có hàm này
        model.addAttribute("user", user);
        List<Orders> orders = orderService.getByUserId(user.getUserId().toString());
        model.addAttribute("orders", orders);
        return "UserProfile"; // view hiển thị profile cá nhân
    }

    // Tạo mới user (nếu dùng form)
    @PostMapping("")
    public String postUser(@ModelAttribute User entity, Model model) {
        if (userService.getUserById(entity.getUserId().toString()) != null) {
            entity.setImgPath(userService.getUserById(entity.getUserId().toString()).getImgPath());
        }
        userService.save(entity);
        model.addAttribute("message", "User created successfully");
        return "redirect:/user/profile";
    }

    // Upload file avatar cho user hiện tại
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile,
            Principal principal,
            Model model) {

        String username = principal.getName();
        User user = userService.findByUsername(username);
        userService.saveFile(multipartFile, user.getUserId().toString());

        model.addAttribute("message", "Upload thành công!");
        return "redirect:/user/profile"; // quay lại trang profile sau khi upload
    }

    @PostMapping("/change-password")
    public String postMethodName(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        // 1. Kiểm tra mật khẩu hiện tại
        if (!encoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/auth/change-password";
        }

        // 2. Đối chiếu new vs confirm
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận chưa khớp.");
            return "redirect:/auth/change-password";
        }

        // 3. Mã hoá và cập nhật mật khẩu mới
        String encodedNew = encoder.encode(newPassword);
        user.setPassword(encodedNew);
        userService.save(user);
        return "redirect:/auth/logout";
    }

}
