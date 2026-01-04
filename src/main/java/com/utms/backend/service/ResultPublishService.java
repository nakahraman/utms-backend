package com.utms.backend.service;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.entities.Notification;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.EvaluationRepository;
import com.utms.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResultPublishService {

    private final ApplicationRepository applicationRepository;
    private final EvaluationRepository evaluationRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public ResultPublishService(ApplicationRepository applicationRepository,
                                EvaluationRepository evaluationRepository,
                                NotificationRepository notificationRepository,
                                EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.evaluationRepository = evaluationRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public void publishResults() {
        // Sadece bekleyen başvuruları alıyoruz
        List<Application> apps = applicationRepository.findByStatus(ApplicationStatus.SENT_TO_REGISTRAR.toString());

        for (Application app : apps) {
            // PERFORMANS: Tüm listeyi çekmek yerine sadece ilgili başvurunun değerlendirmesini al
            Evaluation ev = evaluationRepository.findByApplication_AppId(app.getAppId())
                    .orElse(null);

            if (ev == null) continue;

            // 1. Durum Güncelleme
            updateAppStatus(app, ev);
            applicationRepository.save(app);

            // 2. Sistem Bildirimi (Veritabanı Kaydı)
            Notification notif = new Notification();
            notif.setApplication(app);
            notif.setType("RESULT");
            String message = "Başvuru sonucunuz: " + app.getStatus();
            notif.setMessage(message);
            notif.setDateSent(LocalDateTime.now());
            notificationRepository.save(notif);

            // 3. SRS GEREKSİNİMİ: ÇOKLU KANAL (Email / SMS)
            try {
                // Email Gönderimi
                emailService.sendEmail(app.getStudent().getEmail(), "İYTE Yatay Geçiş Başvuru Sonucu", message);

            } catch (Exception e) {
                // Bildirimlerden biri başarısız olsa da döngü devam etmeli
                System.err.println("Bildirim gönderilemedi: " + e.getMessage());
            }
        }
    }

    private void updateAppStatus(Application app, Evaluation ev) {
        if ("Primary".equals(ev.getDecision())) {
            app.setStatus(ApplicationStatus.APPROVED);
        } else if (ApplicationStatus.WAITLISTED.equals(ev.getDecision())) {
            app.setStatus(ApplicationStatus.WAITLISTED);
        } else {
            app.setStatus(ApplicationStatus.REJECTED);
        }
    }
}