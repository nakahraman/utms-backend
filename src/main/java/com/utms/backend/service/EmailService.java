package com.utms.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSafe(String to, String subject, String body) {
        try {
            send(to, subject, body);
        } catch (Exception e) {
            System.err.println("E-posta gönderilemedi [" + to + "]: " + e.getMessage());
        }
    }

    private void send(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("oidb@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email başarıyla gönderildi: " + to);
    }
}