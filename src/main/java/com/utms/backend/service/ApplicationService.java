package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalVerificationClient;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.entities.Student;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.ValidationStatus;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.DepartmentRepository;
import com.utms.backend.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final ExternalVerificationClient externalClient;
    private final TransferDocumentService documentService;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              StudentRepository studentRepository,
                              DepartmentRepository departmentRepository,
                              ExternalVerificationClient externalClient,
                              TransferDocumentService documentService,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.externalClient = externalClient;
        this.documentService = documentService;
        this.notificationService = notificationService;
    }

    public Application submitApplication(Long studentId, Long departmentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("STD-404", "Öğrenci bulunamadı."));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException("DPT-404", "Bölüm bulunamadı."));

        Application application = new Application();
        application.setStudent(student);
        application.setDepartment(department);
        application.setGpa(student.getGpa());
        application.setSubmissionDate(LocalDateTime.now());
        application.setStatus(ApplicationStatus.SUBMITTED);

        // SRS: External öğrenci otomatik VALID olamaz
        if (student.getStudentType() == StudentType.EXTERNAL) {
            application.setValidationStatus(ValidationStatus.FLAGGED);
        }

        Application saved = applicationRepository.save(application);

        // UC-002 – Submit sonrası otomatik bildirim
        try {
            String msg = "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru numaranız: "
                         + saved.getAppId();

            notificationService.create(saved, "SUBMIT", msg);

        } catch (Exception e) {
            System.err.println("Başvuru bildirimi gönderilemedi: " + e.getMessage());
        }

        return saved;
    }

    public List<Application> getApplicationsByStudent(Long studentId) {
        return applicationRepository.findByStudent_StudentId(studentId);
    }

    public List<Application> getSubmittedApplications() {
        return getApplicationsByStatus(ApplicationStatus.SUBMITTED);
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status.name());
    }

    public Application validateApplication(Long appId, boolean valid) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        documentService.validateMandatoryDocuments(app);

        // 🔴 EXTERNAL öğrenci → YDYO akışına girer
        if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {

            boolean hasCert =
                    documentService.hasDocument(app.getAppId(), DocumentType.ENGLISH_CERTIFICATE);

            app.setStatus(ApplicationStatus.SENT_TO_YDYO);
            app.setValidationStatus(ValidationStatus.FLAGGED);

            String msg = hasCert
                    ? "İngilizce belgeniz YDYO tarafından değerlendirilecektir."
                    : "İngilizce belgeniz bulunamadı. YDYO seviye tespit sınavına yönlendirildiniz.";

            notificationService.create(app, "ENGLISH_PREP", msg);

            return applicationRepository.save(app);
        }

        // 🔵 INTERNAL öğrenci → otomatik doğrulama
        String studentNo = app.getStudent().getStudentId().toString();

        if (!externalClient.verifyExamScore(studentNo)
            || !externalClient.verifyEnglishProficiency(studentNo)) {

            app.setValidationStatus(ValidationStatus.FLAGGED);
            app.setStatus(ApplicationStatus.RETURNED);
            return applicationRepository.save(app);
        }

        // 🧑‍💼 ÖİDB manuel kararı (INTERNAL)
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
        return applicationRepository.findByStatusIn(
                List.of(ApplicationStatus.VALIDATED, ApplicationStatus.YDYO_APPROVED)
        );
    }

    public Application sendToDepartment(Long appId) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        app.setStatus(ApplicationStatus.SENT_TO_DEPARTMENT);
        return applicationRepository.save(app);
    }

    @Transactional
    public void finalizeApplicationResult(Long appId, Evaluation ev) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        updateAppStatus(app, ev);  // private helper burada taşınacak
        applicationRepository.save(app);
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
