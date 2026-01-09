package com.utms.backend.service;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Notification;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.NotificationType;
import com.utms.backend.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void create(Application app, ApplicationStatus type, String message) {

        Notification notif = new Notification();
        notif.setApplication(app);
        notif.setType(type.name());
        notif.setMessage(message);
        notif.setDateSent(LocalDateTime.now());
        notificationRepository.save(notif);

        if (type == ApplicationStatus.RESULT_PUBLISHED) {
            emailService.sendSafe(app, message);
        }
    }

}
