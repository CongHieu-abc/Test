package com.swp.myleague.model.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.swp.myleague.model.entities.match.Match;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendMail(String from, String to, String subject, String text, byte[] imageBytes) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            if (imageBytes != null) {
                helper.addInline("qrImage", new ByteArrayResource(imageBytes), "image/png");
            }

            
            mailSender.send(message);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void sendLineupReminder(String toEmail, String clubName, Match match, String timeNotice) {
        SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("⚠️ Reminder: Submit Your Lineup - " + timeNotice);
    message.setText("Dear " + clubName + ",\n\n" +
            "This is a reminder to submit your **starting lineup** for the upcoming match:\n\n" +
            "Match: " + match.getMatchTitle() + "\n" +
            "Kickoff Time: " + match.getMatchStartTime() + "\n" +
            "Reminder Time: " + timeNotice + "\n\n" +
            "Please submit the lineup as soon as possible.\n\n" +
            "Thank you,\nMyLeague Admin");
    mailSender.send(message);
    }

}
