package com.utms.backend.service;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.eligibility.internalStudent.AcademicEligibilityEvaluator;
import com.utms.backend.eligibility.internalStudent.ExternalAcademicSnapshotClient;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.ExternalVerificationClient;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.DepartmentCriteriaDto;
import com.utms.backend.model.entities.*;
import com.utms.backend.model.enums.*;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.DepartmentRepository;
import com.utms.backend.repository.StudentRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
    private final ExternalAcademicSnapshotClient academicSnapshotClient;
    private final AcademicEligibilityEvaluator eligibilityEvaluator;
    private final ExternalEligibilityExtractor externalEligibilityExtractor;
    private final EnglishScoreService englishScoreService;


    @Transactional
    public ApplicationResponseDto submitApplication(Long studentId, Long departmentId) {

        Student student = findStudent(studentId);
        Department department = findDepartment(departmentId);
        checkDuplicate(studentId, departmentId);

        Application app = createDraftApplication(student, department);

        if (student.getStudentType() == StudentType.INTERNAL) {
            handleInternalStudentFlow(app, department);
        }

        if (student.getStudentType() == StudentType.EXTERNAL) {
            handleExternalStudentFlow(app, department);
        }

        sendSubmitNotification(app);
        return applicationMapper.map(app);
    }


    private Application createDraftApplication(Student student, Department department) {

        Application app = new Application();
        app.setStudent(student);
        app.setDepartment(department);
        app.setGpa(student.getGpa());
        app.setSubmissionDate(LocalDateTime.now());
        app.setStatus(ApplicationStatus.DRAFT);

        return applicationRepository.save(app);
    }

    private void handleInternalStudentFlow(Application app, Department department) {

        AcademicEligibilitySnapshot snapshot =
                academicSnapshotClient.fetchSnapshot(
                        app.getStudent().getStudentId().toString(),
                        department.getDeptId()
                );

        DepartmentCriteriaDto criteria = department.getCriteria().toDto();

        if (!eligibilityEvaluator.isEligible(snapshot, criteria)) {

            transitionService.transition(app, ApplicationStatus.REJECTED,
                    "Internal academic criteria not met");

            throw new BusinessException("APP-ELIG-001",
                    "Başvuru kriterleri karşılanmadığı için başvurunuz reddedildi.");
        }

        transitionService.transition(app, ApplicationStatus.SUBMITTED,
                "Internal academic eligibility passed");
    }

    private void handleExternalStudentFlow(Application app, Department department) {

        // Zorunlu belgeler: TRANSCRIPT + YKS_RESULT
        documentService.validateMandatoryDocuments(app);

        AcademicEligibilitySnapshot snapshot =
                externalEligibilityExtractor.extract(app);

        DepartmentCriteriaDto criteria = department.getCriteria().toDto();

        if (!eligibilityEvaluator.isEligible(snapshot, criteria)) {

            transitionService.transition(app, ApplicationStatus.REJECTED,
                    "External academic criteria not met");

            throw new BusinessException("APP-ELIG-EXT-001",
                    "Yüklenen belgeler bölüm kriterlerini karşılamadığı için başvurunuz reddedildi.");
        }

        // ❗ İngilizce belgesi burada KONTROL EDİLMEZ
        transitionService.transition(app, ApplicationStatus.SUBMITTED,
                "External academic eligibility passed");
    }


    private void sendSubmitNotification(Application app) {

        notificationService.create(app,
                "SUBMIT",
                "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru No: " + app.getAppId());
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STD-404", "Öğrenci bulunamadı."));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("DPT-404", "Bölüm bulunamadı."));
    }

    private void checkDuplicate(Long studentId, Long departmentId) {
        if (applicationRepository.existsByStudent_StudentIdAndDepartment_DeptId(studentId, departmentId)) {
            throw new BusinessException("APP-409", "Bu bölüme daha önce başvuru yaptınız.");
        }
    }


    public List<ApplicationResponseDto> getApplicationsByStudent(Long studentId) {
        return applicationRepository.findAllByStudentWithRelations(studentId)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<ApplicationResponseDto> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<ApplicationResponseDto> getFacultyEvaluatedApplicationsforDepartments() {
        return applicationRepository.findByStatus(ApplicationStatus.SENT_TO_YGK)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<ApplicationResponseDto> getFacEvaluatedApplicationsForYgk(ApplicationStatus status) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> getApplicationsByStatusAndFaculty(List<ApplicationStatus> statuses, Long facultyId) {
        return applicationRepository.findFacultyInbox(statuses, facultyId);
    }

    @Transactional
    public ApplicationResponseDto validateApplication(Long appId, boolean valid) {

        Application app = findApplicationById(appId);

        // 🔒 OIDB yalnızca YDYO sonucu gelmiş başvurularla çalışır
        if (app.getStatus() != ApplicationStatus.YDYO_APPROVED &&
            app.getStatus() != ApplicationStatus.YDYO_FAILED) {

            throw new BusinessException("OIDB-403",
                    "Only applications evaluated by YDYO can be validated by OIDB");
        }

        // ❌ YDYO'dan kalan otomatik reddedilir
        if (app.getStatus() == ApplicationStatus.YDYO_FAILED) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.REJECTED,
                    "Application rejected due to YDYO failure"
            );

            return applicationMapper.map(updated);
        }

        // ✅ Buraya gelen herkes YDYO_APPROVED
        documentService.validateMandatoryDocuments(app);

        if (!valid) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.REJECTED,
                    "OIDB rejected application after YDYO approval"
            );

            return applicationMapper.map(updated);
        }

        // 🎯 OIDB ONAYI
        app.setValidationStatus(ValidationStatus.VALID);

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.OIDB_VALIDATED,
                "OIDB validated application after YDYO approval"
        );

        return applicationMapper.map(updated);
    }

    @Transactional
    public ApplicationResponseDto sendToYgk(Long appId, boolean valid) {

        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.SENT_TO_YGK)
            throw new BusinessException("YGK-401","Sadece bölüm değerlendirmesi bekleyen başvurular YGK'ya gönderilebilir.");

        ApplicationStatus nextStatus = valid
                ? ApplicationStatus.SENT_TO_YGK
                : ApplicationStatus.RETURNED_TO_OIDB;

        String reason = valid
                ? "Department approved and forwarded to YGK"
                : "Department rejected and returned to OIDB";

        Application updated = transitionService.transition(app, nextStatus, reason);

        return applicationMapper.map(updated);
    }

    @Transactional
    public ApplicationResponseDto finalizeApplication(Long appId, Decision decision) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Application not found"));

        if (app.getStatus() != ApplicationStatus.SENT_TO_YGK) {
            throw new BusinessException("YGK-405", "Application is not ready for YGK decision");
        }


        ApplicationStatus target =
                decision == Decision.REJECTED
                        ? ApplicationStatus.REJECTED
                        : ApplicationStatus.APPROVED;

        Application updated = transitionService.transition(
                app,
                target,
                "Final decision given by YGK: " + decision
        );

        return applicationMapper.map(updated);
    }


    public List<ApplicationResponseDto> getOidbInbox(List<OidbStatus> statuses) {

        List<OidbStatus> effectiveStatuses =
                (statuses == null || statuses.isEmpty())
                        ? List.of(
                        OidbStatus.SUBMITTED,
                        OidbStatus.YDYO_APPROVED,
                        OidbStatus.YDYO_FAILED,
                        OidbStatus.FACULTY_EVALUATED,
                        OidbStatus.FACULTY_RETURNED,
                        OidbStatus.YGK_APPROVED,
                        OidbStatus.YGK_REJECTED
                )
                        : statuses;
        return applicationRepository.findByStatusInWithRelations(statuses)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    private ApplicationStatus decideFinalStatus(Evaluation ev) {
        if ("Primary".equals(ev.getDecision())) return ApplicationStatus.APPROVED;
        if ("Waitlisted".equals(ev.getDecision())) return ApplicationStatus.WAITLISTED;
        return ApplicationStatus.REJECTED;
    }

    public Application findApplicationById(Long appId) {
        return applicationRepository.findByIdWithRelations(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));
    }

    public List<Application> findBYStatusIn(List<ApplicationStatus> statuses) {
        return applicationRepository
                .findByStatusIn(statuses);
    }

    public List<Application> getDeptEvaluatedApplications(ApplicationStatus status, Long facultyId) {
        return applicationRepository
                .findByStatusAndDepartment_Faculty_FacultyId(
                        status,
                        facultyId
                );
    }

    public List<Application> getDeptEvApplications(ApplicationStatus status, Long facultyId) {
        return applicationRepository
                .findByStatusAndDepartment_Faculty_FacultyId(
                        status,
                        facultyId
                );
    }

    public List<ApplicationResponseDto> findByStatusInAndFacultyId(List<ApplicationStatus> statuses, Long facultyId) {
        return applicationRepository
                .findFacultyInbox(statuses, facultyId)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> findByStatusInAndFacultyIdForEval(List<ApplicationStatus> statuses, Long facultyId) {
        return applicationRepository
                .findFacultyInbox(statuses, facultyId);
    }

    public ApplicationResponseDto getMyResult() {

        Long studentId = SecurityUtil.getCurrentStudentId();

        Application app = applicationRepository
                .findByStudentStudentIdAndStatus(studentId,
                        ApplicationStatus.RESULT_PUBLISHED)
                .orElseThrow(() -> new BusinessException("RES-404", "Sonuç bulunamadı"));

        return applicationMapper.map(app);
    }

    public List<ApplicationResponseDto> getPublishedResults() {

        return applicationRepository
                .findByStatusWithStudent(ApplicationStatus.RESULT_PUBLISHED)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    @Transactional
    public ApplicationResponseDto validateYdyo(Long appId) {

        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.SENT_TO_YDYO)
            throw new BusinessException("YDYO-401",
                    "Only YDYO routed applications can be validated");

        boolean hasCert =
                documentService.hasDocument(app.getAppId(), DocumentType.ENGLISH_CERTIFICATE);

        if (!hasCert) {

            return applicationMapper.map(
                    transitionService.transition(app,
                            ApplicationStatus.YDYO_EXAM_REQUIRED,
                            "No certificate. Placement exam required")
            );
        }

        EnglishCertificate cert = documentService.getEnglishCertificate(app.getAppId());

        boolean passed = englishScoreService.isValid(cert);

        if (!passed) {

            return applicationMapper.map(
                    transitionService.transition(app,
                            ApplicationStatus.YDYO_EXAM_REQUIRED,
                            "Certificate score insufficient. Placement exam required")
            );
        }

        return applicationMapper.map(
                transitionService.transition(app,
                        ApplicationStatus.YDYO_APPROVED,
                        "English proficiency accepted by certificate")
        );
    }

    @Transactional
    public ApplicationResponseDto submitPlacementExamResult(Long appId, boolean passed) {

        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.YDYO_EXAM_REQUIRED)
            throw new BusinessException("YDYO-402",
                    "Only exam required applications can be finalized");

        ApplicationStatus target = passed
                ? ApplicationStatus.YDYO_APPROVED
                : ApplicationStatus.YDYO_FAILED;

        String reason = passed
                ? "Placement exam passed"
                : "Placement exam failed";

        return applicationMapper.map(
                transitionService.transition(app, target, reason)
        );
    }

}

