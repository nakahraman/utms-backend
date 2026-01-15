package com.utms.backend.service;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.mapper.ApplicationStatusHistoryMapper;
import com.utms.backend.mapper.StudentMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.ApplicationStatusHistoryDto;
import com.utms.backend.model.dto.StudentProfileDto;
import com.utms.backend.model.entities.*;
import com.utms.backend.model.enums.*;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusHistory;
import com.utms.backend.statusHistory.ApplicationStatusHistoryRepository;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DepartmentService departmentService;
    private final DocumentService documentService;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;
    private final ExternalEligibilityExtractor externalEligibilityExtractor;
    private final EnglishCertificateService englishCertificateService;
    private final EvaluationService evaluationService;
    private final StudentService studentService;
    private final StudentMapper studentMapper;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationStatusHistoryMapper historyMapper;



    // ---------- DRAFT ----------
    @Transactional
    public Long createDraft(Long userId, Long deptId) {

        Student student = studentService.resolveStudent(userId);
        Department dept = departmentService.findDepartmentById(deptId);

        checkCanSubmit(student.getStudentId(), dept.getDeptId());

        Application app = Application.builder()
                .student(student)
                .department(dept)
                .status(ApplicationStatus.DRAFT)
                .createdDate(LocalDateTime.now())
                .validationStatus(ValidationStatus.FLAGGED)
                .status(ApplicationStatus.DRAFT)
                .published(false)
                .build();

        return applicationRepository.save(app).getAppId();
    }

    // ---------- EXTERNAL SUBMIT ----------

    @Transactional
    public ApplicationResponseDto submitExternalApplication(Long appId) {

        Application app = validateDraftSubmissionOwnership(appId);

        documentService.validateMandatoryDocuments(app);

        AcademicEligibilitySnapshot snapshot = externalEligibilityExtractor.extract(app);
        app.setGpa(snapshot.getGpa());

        applyExternalAcademicSnapshotToStudent(app, snapshot);

        finalizeSubmission(app, "External application submitted");

        return applicationMapper.map(app);
    }


    @Transactional
    public void applyExternalAcademicSnapshotToStudent(Application app, AcademicEligibilitySnapshot s) {

        if (app.getStudent().getStudentType() != StudentType.EXTERNAL) return;

        Student st = app.getStudent();
        st.setGpa(s.getGpa());
        st.setExamScore(s.getExamScore());
        st.setSuccessRank(s.getSuccessRank());

        studentService.save(st);
    }

    // ---------- INTERNAL SUBMIT ----------

    @Transactional
    public ApplicationResponseDto submitInternalApplication(Long userId, Long appId) {

        Student student = studentService.findStudentIdByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException("STU-404",
                                "Bu kullanıcıya ait öğrenci profili bulunamadı"));

        Application app = validateDraftSubmissionOwnership(appId);

        validateInternalStudentNotSameDepartment(student, app);

        app.setGpa(student.getGpa());

        finalizeSubmission(app, "Internal application submitted");

        return applicationMapper.map(app);
    }

    private void validateInternalStudentNotSameDepartment(Student student, Application app) {

        if (student.getDepartment() == null || app.getDepartment() == null) {
            throw new BusinessException("APP-INT-000",
                    "Bölüm bilgileri eksik olduğu için başvuru doğrulanamadı.");
        }

        if (student.getDepartment().getDeptId()
                .equals(app.getDepartment().getDeptId())) {

            throw new BusinessException("APP-INT-001",
                    "Kendi bölümünüze yatay geçiş başvurusu yapamazsınız.");
        }
    }


    // ---------- DOMAIN RULES ----------

    private Application validateDraftSubmissionOwnership(Long appId) {

        Long userId = SecurityUtil.getCurrentUserId();
        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.DRAFT)
            throw new BusinessException("APP-400",
                    "Bu başvuru artık düzenlenemez.");

        if (!app.getStudent().getUser().getUserId().equals(userId))
            throw new BusinessException("SEC-403", "Bu başvuru size ait değil");

        if (app.getStatus() != ApplicationStatus.DRAFT)
            throw new BusinessException("APP-400", "Sadece taslak başvurular gönderilebilir");

        return app;
    }

    private void checkCanSubmit(Long studentId, Long deptId) {

        if (applicationRepository.existsByStudent_StudentIdAndDepartment_DeptId(studentId, deptId)) {

            throw new BusinessException("APP-409", "Bu bölüm için devam eden veya sonuçlanmış bir başvurunuz bulunmaktadır.");
        }
    }

    private void finalizeSubmission(Application app, String reason) {

        transitionService.transition(app, ApplicationStatus.SUBMITTED, reason);
    }

    public List<ApplicationResponseDto> getApplicationsByStudent(Long studentId) {

        List<ApplicationResponseDto> list =
                applicationRepository.findAllByStudentWithRelations(studentId)
                        .stream()
                        .map(applicationMapper::map)
                        .toList();

        if (list.isEmpty())
            throw new BusinessException("APP-404",
                    "Henüz hiç başvuru yapmadınız.");

        return list;
    }


    public List<ApplicationResponseDto> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status)
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

    @Transactional
    public ApplicationResponseDto oidbValidateApplication(Long appId) {

        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.YDYO_APPROVED) {

            throw new BusinessException("OIDB-403",
                    "Only applications approved by YDYO can be validated by OIDB");
        }

        try {
            documentService.validateMandatoryDocuments(app);
        } catch (BusinessException ex) {

            transitionService.transition(
                    app,
                    ApplicationStatus.OIDB_FLAGGED,
                    ex.getMessage()
            );

            throw new BusinessException("OIDB-VAL-001",
                    "Application flagged due to missing or invalid documents");
        }

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.OIDB_VALIDATED,
                "OIDB validation completed – forwarded to Faculty"
        );

        return applicationMapper.map(updated);
    }


    @Transactional
    public ApplicationResponseDto finalizeApplication(Long appId, Decision decision) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Application not found"));

        if (app.getStatus() != ApplicationStatus.SENT_TO_YGK) {
            throw new BusinessException("YGK-405",
                    "Only applications sent to YGK can be finalized");
        }

        Evaluation ev = evaluationService
                .findEvaluationByApplicationId(app.getAppId());

        // 1️⃣ Evaluation güncellenir
        ev.setDecision(decision);
        ev.setYgkMemberId(SecurityUtil.getCurrentUserId());
        evaluationService.save(ev);

        // 2️⃣ Application snapshot decision yazılır
        app.setDecision(decision);

        // 3️⃣ Status transition
        ApplicationStatus target =
                decision == Decision.REJECTED
                        ? ApplicationStatus.YGK_REJECTED
                        : decision == Decision.WAITLISTED
                        ? ApplicationStatus.YGK_WAITLISTED
                        : ApplicationStatus.YGK_APPROVED;

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
                        OidbStatus.YGK_REJECTED,
                        OidbStatus.YGK_WAITLISTED
                )
                        : statuses;
        return applicationRepository.findByStatusInWithRelations(statuses)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<ApplicationResponseDto> getFacultyInbox(List<ApplicationStatus> statuses, Long facultyId) {
        return applicationRepository
                .getFacultyInbox(statuses, facultyId)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> getFacultyApplicationsForEvaluation(List<ApplicationStatus> statuses, Long facultyId) {
        return applicationRepository
                .getFacultyInbox(statuses, facultyId);
    }

    public ApplicationResponseDto getMyResult() {

        Long studentId = SecurityUtil.getCurrentStudentId();

        Application app = applicationRepository
                .findByStudentStudentIdAndStatus(studentId,
                        ApplicationStatus.RESULT_PUBLISHED)
                .orElseThrow(() -> new BusinessException("RES-404", "Sonuç bulunamadı"));

        return applicationMapper.map(app);
    }

    public Application findApplicationById(Long appId) {
        return applicationRepository.findByIdWithRelations(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));
    }

    @Transactional
    public ApplicationResponseDto validateYdyo(Long appId) {

        Application app = findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.SENT_TO_YDYO)
            throw new BusinessException("YDYO-401",
                    "Only YDYO routed applications can be validated");


        // 🔵 INTERNAL öğrenciler doğrudan YDYO_VALIDATED
        if (app.getStudent().getStudentType() == StudentType.INTERNAL) {
            app.setEnglishResult(EnglishProficiencyResult.PASSED);

            return applicationMapper.map(
                    transitionService.transition(
                            app,
                            ApplicationStatus.YDYO_APPROVED,
                            "Internal student – English proficiency assumed valid"
                    )
            );
        }

        //EXTERNAL STUDENT
        Optional<EnglishCertificate> certOpt =
                englishCertificateService.getEnglishCertificate(app.getAppId());

        if (certOpt.isEmpty()) {

            app.setEnglishResult(EnglishProficiencyResult.EXAM_REQUIRED);

            return applicationMapper.map(
                    transitionService.transition(app,
                            ApplicationStatus.YDYO_EXAM_REQUIRED,
                            "No certificate. Placement exam required")
            );
        }

        EnglishCertificate cert = certOpt.get();
        boolean passed = englishCertificateService.isValid(cert);

        if (!passed) {
            app.setEnglishResult(EnglishProficiencyResult.EXAM_REQUIRED);
            return applicationMapper.map(
                    transitionService.transition(app,
                            ApplicationStatus.YDYO_EXAM_REQUIRED,
                            "Certificate score insufficient. Placement exam required")
            );
        }

        app.setEnglishResult(EnglishProficiencyResult.PASSED);
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

        app.setEnglishResult(passed
                ? EnglishProficiencyResult.PASSED
                : EnglishProficiencyResult.FAILED);

        return applicationMapper.map(
                transitionService.transition(app,
                        ApplicationStatus.YDYO_APPROVED,
                        passed ? "Placement exam passed"
                                : "Placement exam failed")
        );
    }


    public List<ApplicationResponseDto> getFinalizedResults(Boolean published) {

        List<ApplicationStatus> terminalStatuses = List.of(
                ApplicationStatus.YGK_APPROVED,
                ApplicationStatus.YGK_REJECTED,
                ApplicationStatus.YGK_WAITLISTED
        );

        return applicationRepository
                .findFinalizedResultsForView(terminalStatuses, published)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> findPublishableResults() {

        List<ApplicationStatus> publishable = List.of(
                ApplicationStatus.YGK_APPROVED,
                ApplicationStatus.YGK_WAITLISTED,
                ApplicationStatus.YGK_REJECTED,
                ApplicationStatus.ACADEMICALLY_INELIGIBLE
        );

        return applicationRepository.findByStatusInAndPublishedFalse(publishable);
    }


    public ApplicationResponseDto getLatestMyApplication() {

        Long studentId = SecurityUtil.getCurrentStudentId();

        Application app = applicationRepository
                .findTopByStudentStudentIdOrderByCreatedDateDesc(studentId)
                .orElseThrow(() ->
                        new BusinessException("APP-404", "Henüz hiç başvuru yapmadınız."));

        return applicationMapper.map(app);
    }


    public StudentProfileDto getMyStudentProfile(Long studentId) {

        Student student = studentService.findStudentIdByStudentId(studentId);

        return studentMapper.map(student);
    }


    public ApplicationResponseDto getMyApplicationById(Long studentId, Long appId) {

        Application app = applicationRepository
                .findByAppIdAndStudent_StudentId(appId, studentId)
                .orElseThrow(() ->
                        new BusinessException("APP-404", "Başvuru bulunamadı."));

        return applicationMapper.map(app);
    }

    public List<ApplicationStatusHistoryDto> getMyApplicationHistory(Long appId) {

        List<ApplicationStatusHistory> list =
                historyRepository.findByApplication_AppId(appId);

        return list.stream()
                .map(historyMapper::map)
                .toList();
    }

}

