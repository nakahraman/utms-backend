package com.utms.backend.service;

import com.utms.backend.model.entities.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    public void sendSafe(Application app, String decisionMessage) {

        if (!mailEnabled) return;

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(app.getStudent().getUser().getEmail());
            msg.setSubject("UTMS - Yatay Geçiş Başvuru Sonucu");
            msg.setText(buildResultMailBody(app, decisionMessage));
            mailSender.send(msg);

        } catch (Exception ex) {

            System.out.println("EMAIL-SEND-FAILED"
                               + "\n to= " + app.getStudent().getUser().getEmail()
                               + "\n subject= UTMS - Yatay Geçiş Başvuru Sonucu"
                               + "\n" + ex);
        }
    }

    private String buildResultMailBody(Application app, String decisionMessage) {

        String studentName = app.getStudent().getUser().getName();
        String departmentName = app.getDepartment().getDeptName();
        String submitDate = app.getSubmissionDate()
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        Long appId = app.getAppId();

        return """
            Sayın %s,

            %s tarihinde %s bölümüne yapmış olduğunuz % numaralı yatay geçiş başvurunuzun sonucu aşağıda bilgilerinize sunulmuştur.

            Başvuru Sonucu:
            %s

            Detaylara UTMS sistemi üzerinden erişebilirsiniz.

            İyi çalışmalar dileriz.
            İYTE Öğrenci İşleri Daire Başkanlığı
            """.formatted(studentName, submitDate, departmentName, appId, decisionMessage);
    }
}
