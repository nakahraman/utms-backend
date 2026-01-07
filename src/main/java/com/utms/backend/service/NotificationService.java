package com.utms.backend.service;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Notification;
import com.utms.backend.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void create(Application app, String type, String message) {

        Notification notif = new Notification();
        notif.setApplication(app);
        notif.setType(type);
        notif.setMessage(message);
        notif.setDateSent(LocalDateTime.now());
        notificationRepository.save(notif);

        emailService.sendSafe(
                app.getStudent().getEmail(),
                resolveSubject(type),
                message
        );
    }

    private String resolveSubject(String type) {
        return switch (type) {
            case "SUBMIT"        -> "İYTE Yatay Geçiş Başvurunuz Alındı";
            case "RESULT"        -> "İYTE Yatay Geçiş Başvuru Sonucu";
            default              -> "İYTE Başvuru Bilgilendirme";
        };
    }
}
