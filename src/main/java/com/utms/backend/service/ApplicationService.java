package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalVerificationClient;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.DepartmentRepository;
import com.utms.backend.repository.NotificationRepository;
import com.utms.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import com.utms.backend.model.entities.Notification;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final ExternalVerificationClient externalClient;
    private final EmailService emailService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              StudentRepository studentRepository,
                              DepartmentRepository departmentRepository,
                              ExternalVerificationClient externalClient,
                              NotificationRepository notificationRepository,
                              EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.externalClient = externalClient;
        this.notificationRepository =notificationRepository;
        this.emailService = emailService;
    }

    public Application submitApplication(Long studentId, Long departmentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->  new BusinessException("STD-404", "Öğrenci bulunamadı."));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException("DPT-404", "Bölüm bulunamadı."));

        Application application = new Application();
        application.setStudent(student);
        application.setDepartment(department);
        application.setGpa(student.getGpa());
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmissionDate(LocalDateTime.now());

        Application saved = applicationRepository.save(application);

        // UC-002 – Otomatik başvuru alındı bildirimi
        try {
            String msg = "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru numaranız: "
                         + saved.getAppId();

            // Sistem mesajı
            Notification notif = new Notification();
            notif.setApplication(saved);
            notif.setType("SUBMIT");
            notif.setMessage(msg);
            notif.setDateSent(LocalDateTime.now());
            notificationRepository.save(notif);


            // Email bildirimi (SRS zorunlu)
            emailService.sendEmail(saved.getStudent().getEmail(),
                    "İYTE Yatay Geçiş Başvurunuz Alındı",
                    msg);

        } catch (Exception e) {
            System.err.println("Başvuru bildirimi gönderilemedi: " + e.getMessage());
        }

        return saved;

    }

    public List<Application> getApplicationsByStudent(Long studentId) {

        return applicationRepository.findByStudent_StudentId(studentId);
    }

    public List<Application> getSubmittedApplications() {
        return applicationRepository.findByStatus("Submitted");
    }

    public Application validateApplication(Long appId, boolean valid) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        String studentNo = app.getStudent().getStudentId().toString();

        // 🔗 External system verification – UC-005’in ilk adımı
        if (!externalClient.verifyExamScore(studentNo)
            || !externalClient.verifyEnglishProficiency(studentNo)) {

            app.setValidationStatus(ValidationStatus.FLAGGED);
            app.setStatus(ApplicationStatus.RETURNED);
            return applicationRepository.save(app);
        }

        // 🧑‍💼 ÖİDB manuel kararı – dış doğrulamalar geçtiyse
        if (valid) {
            app.setValidationStatus(ValidationStatus.VALID);
            app.setStatus(ApplicationStatus.VALIDATED);
        } else {
            app.setValidationStatus(ValidationStatus.FLAGGED);
            app.setStatus(ApplicationStatus.RETURNED);
        }

        return applicationRepository.save(app);
    }


    public List<Application> getValidatedApplicationsForFaculty() {

        return applicationRepository.findByStatusAndValidationStatus("Validated", "Valid");
    }

    public Application sendToDepartment(Long appId) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        app.setStatus(ApplicationStatus.SENT_TO_DEPARTMENT);

        return applicationRepository.save(app);
    }


}
