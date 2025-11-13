package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.swp.myleague.model.service.EmailService;

@RequestMapping(value = "/feedback")
@Controller
public class FeedbackController {

    @Autowired
    EmailService emailService;

    @GetMapping("")
    public String getFeedbackPage() {
        return "FeedbackPage";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> handleFeedbackSubmission(@RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam String message) {
        System.out.println("Received feedback:");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Message: " + message);

        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color:#30003a;'>Cảm ơn bạn đã gửi phản hồi!</h2>" +
                "<p>Chúng tôi đã nhận được phản hồi của bạn:</p>" +
                "<hr>" +
                "<p><strong>Tên:</strong> " + (name != null ? name : "Không cung cấp") + "</p>" +
                "<p><strong>Email:</strong> " + (email != null ? email : "Không cung cấp") + "</p>" +
                "<p><strong>Nội dung:</strong><br/>" + message + "</p>" +
                "<hr>" +
                "<p>Chúng tôi sẽ xem xét và liên hệ lại nếu cần thêm thông tin.</p>" +
                "<p style='color:gray;font-size:12px;'>Premier League Support Team</p>" +
                "</body>" +
                "</html>";

                emailService.sendMail("chumlu2102@gmail.com", email, "[FEEDBACK USER]", htmlContent, null);
                emailService.sendMail("chumlu2102@gmail.com", "phantrunghieu0000@gmail.com", "[FEEDBACK USER]", htmlContent, null);
        return ResponseEntity.ok("Đã nhận phản hồi");
    }

}
