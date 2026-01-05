package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalVerificationClient;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
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
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final ExternalVerificationClient externalClient;
    private final TransferDocumentService documentService;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;


    public ApplicationResponseDto submitApplication(Long studentId, Long departmentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("STD-404", "Öğrenci bulunamadı."));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException("DPT-404", "Bölüm bulunamadı."));

        if (applicationRepository.existsByStudent_StudentIdAndDepartment_DeptId(studentId, departmentId)) {
            throw new BusinessException(
                    "APP-409",
                    "Bu bölüme daha önce başvuru yaptınız."
            );
        }

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

        try {
            // 2️⃣ Mutlak güvenlik (race-condition kilidi)
            Application saved = applicationRepository.save(application);

            try {
                String msg = "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru numaranız: " + saved.getAppId();
                notificationService.create(saved, "SUBMIT", msg);
            } catch (Exception e) {
                System.err.println("Başvuru bildirimi gönderilemedi: " + e.getMessage());
            }

            return applicationMapper.map(saved);

        } catch (DataIntegrityViolationException e) {
            // Aynı anda iki submit geldi → DB unique constraint yakaladı
            throw new BusinessException("APP-409", "Bu bölüme daha önce başvuru yaptınız.");
        }
    }

    public List<ApplicationResponseDto> getApplicationsByStudent(Long studentId) {
        return applicationRepository.findAllByStudentWithRelations(studentId)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<ApplicationResponseDto> getSubmittedApplications() {

        return applicationRepository.findByStatus(ApplicationStatus.SUBMITTED)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {

        return applicationRepository.findByStatus(status);
    }

    public ApplicationResponseDto validateApplication(Long appId, boolean valid) {

        Application app = findApplicationById(appId);

        documentService.validateMandatoryDocuments(app);

        // 🔴 EXTERNAL öğrenci → YDYO akışına girer
        if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {

            boolean hasCert =
                    documentService.hasDocument(app.getAppId(), DocumentType.ENGLISH_CERTIFICATE);

            app.setValidationStatus(ValidationStatus.FLAGGED);

            String msg = hasCert
                    ? "İngilizce belgeniz YDYO tarafından değerlendirilecektir."
                    : "İngilizce belgeniz bulunamadı. YDYO seviye tespit sınavına yönlendirildiniz.";

            notificationService.create(app, "ENGLISH_PREP", msg);

            Application updated = transitionService.transition(app, ApplicationStatus.SENT_TO_YDYO,
                    "External student routed to YDYO");
            return applicationMapper.map(updated);
        }

        // 🔵 INTERNAL öğrenci → otomatik doğrulama
        String studentNo = app.getStudent().getStudentId().toString();

        if (!externalClient.verifyExamScore(studentNo)
            || !externalClient.verifyEnglishProficiency(studentNo)) {

            app.setValidationStatus(ValidationStatus.FLAGGED);
            Application updated = transitionService.transition(app, ApplicationStatus.RETURNED,
                    "Internal validation failed");

            return applicationMapper.map(updated);
        }

        // 🧑‍💼 ÖİDB manuel kararı (INTERNAL)
        if (valid) {
            app.setValidationStatus(ValidationStatus.VALID);
            Application updated = transitionService.transition(app, ApplicationStatus.VALIDATED,
                    "Registrar validated internal student");
            return applicationMapper.map(updated);

        } else {
            app.setValidationStatus(ValidationStatus.FLAGGED);
            Application updated = transitionService.transition(app, ApplicationStatus.RETURNED,
                    "Registrar returned internal student");
            return applicationMapper.map(updated);
        }
    }

    public List<ApplicationResponseDto> getValidatedApplicationsForFaculty() {

        List<ApplicationStatus> statuses =
                List.of(ApplicationStatus.VALIDATED, ApplicationStatus.YDYO_APPROVED);

        return applicationRepository.findByStatusInWithRelations(statuses)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public ApplicationResponseDto sendToDepartment(Long appId) {

        Application app = findApplicationById(appId);

        Application updated = transitionService.transition(app, ApplicationStatus.SENT_TO_DEPARTMENT,
                "Faculty forwarded to department");

        return applicationMapper.map(updated);
    }

    @Transactional
    public void finalizeApplicationResult(Long appId, Evaluation ev) {

        Application app = findApplicationById(appId);

        ApplicationStatus status = decideFinalStatus(ev);
        transitionService.transition(app, status, "Final evaluation published");
    }

    private ApplicationStatus decideFinalStatus(Evaluation ev) {
        if ("Primary".equals(ev.getDecision())) return ApplicationStatus.APPROVED;
        if ("Waitlisted".equals(ev.getDecision())) return ApplicationStatus.WAITLISTED;
        return ApplicationStatus.REJECTED;
    }

    public Application findApplicationById(Long appId){
        return applicationRepository.findByIdWithRelations(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));
    }

    public List<Application> getDeptEvaluatedApplications(ApplicationStatus status, Long facultyId) {
        return applicationRepository
                .findByStatusAndDepartment_Faculty_FacultyId(
                        status,
                        facultyId
                );
    }
}
