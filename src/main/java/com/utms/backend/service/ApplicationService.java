package com.utms.backend.service;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.eligibility.internalStudent.AcademicEligibilityEvaluator;
import com.utms.backend.eligibility.internalStudent.ExternalAcademicSnapshotClient;
import com.utms.backend.eligibility.internalStudent.InternalEligibilityExtractor;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.DepartmentCriteriaDto;
import com.utms.backend.model.entities.*;
import com.utms.backend.model.enums.*;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DepartmentService departmentService;
    private final TransferDocumentService documentService;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;
    private final ExternalAcademicSnapshotClient academicSnapshotClient;
    private final AcademicEligibilityEvaluator eligibilityEvaluator;
    private final ExternalEligibilityExtractor externalEligibilityExtractor;
    private final InternalEligibilityExtractor internalEligibilityExtractor;
    private final EnglishScoreService englishScoreService;
    private final EvaluationService evaluationService;
    private final StudentService studentService;
    private final UserService userService;

    private final List<ApplicationStatus> ALLOWED_FOR_NEW_APPLICATION = List.of(ApplicationStatus.DRAFT);


    // ---------- DRAFT ----------
    @Transactional
    public Long createDraft(Long userId, Long departmentId) {

        Student student = studentService.resolveStudent(userId);
        Department dept = departmentService.findDepartmentById(departmentId);

        Application app = new Application();
        app.setStudent(student);
        app.setDepartment(dept);
        app.setSubmissionDate(LocalDateTime.now());
        app.setStatus(ApplicationStatus.DRAFT);

        return applicationRepository.save(app).getAppId();
    }


    // ---------- EXTERNAL SUBMIT ----------

    @Transactional
    public ApplicationResponseDto submitExternalApplication(Long appId) {

        Application app = authorize(appId);

        checkCanSubmit(app.getStudent().getStudentId(), app.getDepartment().getDeptId());

        documentService.validateMandatoryDocuments(app);

        AcademicEligibilitySnapshot snapshot = externalEligibilityExtractor.extract(app);
        app.setGpa(snapshot.getGpa());

        finalizeSubmission(app, "External application submitted");

        sendSubmitNotification(app);

        return applicationMapper.map(app);
    }

    private Application authorize(Long appId) {

        Long userId = SecurityUtil.getCurrentUserId();
        Application app = findApplicationById(appId);

        if (!app.getStudent().getUser().getUserId().equals(userId))
            throw new BusinessException("SEC-403", "Bu başvuru size ait değil");

        if (app.getStatus() != ApplicationStatus.DRAFT)
            throw new BusinessException("APP-400", "Sadece taslak başvurular gönderilebilir");

        return app;
    }


    // ---------- INTERNAL SUBMIT ----------

    @Transactional
    public ApplicationResponseDto submitInternalApplication(Long userId, Long appId) {

        Student student = studentService.findStudentIdByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException("STU-404",
                                "Bu kullanıcıya ait öğrenci profili bulunamadı"));

        Application app = authorize(appId);

        app.setGpa(student.getGpa());
        checkCanSubmit(student.getStudentId(), app.getDepartment().getDeptId());

        finalizeSubmission(app, "Internal application submitted");

        sendSubmitNotification(app);

        return applicationMapper.map(app);
    }


    // ---------- DOMAIN RULES ----------

    private void checkCanSubmit(Long studentId, Long deptId) {

        if (applicationRepository.existsByStudent_StudentIdAndDepartment_DeptIdAndStatusNotIn(studentId, deptId, ALLOWED_FOR_NEW_APPLICATION)) {

            throw new BusinessException("APP-409", "Bu bölüm için devam eden veya sonuçlanmış bir başvurunuz bulunmaktadır.");
        }
    }

    private void finalizeSubmission(Application app, String reason) {

        transitionService.transition(app, ApplicationStatus.SUBMITTED, reason);
    }

    private void sendSubmitNotification(Application app) {

        notificationService.create(app, ApplicationStatus.SUBMITTED.toString(),
                "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru No: " + app.getAppId());
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

    public List<ApplicationResponseDto> getFacEvaluatedApplicationsForYgk(ApplicationStatus status) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }
    @Transactional
    public ApplicationResponseDto oidbValidateApplication(Long appId, boolean valid) {

        Application app = findApplicationById(appId);

        // 🔒 OIDB yalnızca YDYO sonucu gelmiş başvurularla çalışır
        if (app.getStatus() != ApplicationStatus.YDYO_APPROVED &&
            app.getStatus() != ApplicationStatus.YDYO_FAILED) {

            throw new BusinessException("OIDB-403",
                    "Only applications evaluated by YDYO can be validated by OIDB");
        }

        // 1️⃣ Belgeler eksik mi?


        AcademicEligibilitySnapshot snapshot;

        if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {
            documentService.validateMandatoryDocuments(app);
            snapshot = externalEligibilityExtractor.extract(app);
        } else {
            snapshot = internalEligibilityExtractor.extract(app);
        }

        DepartmentCriteriaDto criteria =
                app.getDepartment().getCriteria().toDto();

        boolean eligible =
                eligibilityEvaluator.isEligible(snapshot, criteria);

        if (!eligible) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.OIDB_CRITERIA_REJECTED,
                    "OIDB rejected due to academic criteria mismatch"
            );

            return applicationMapper.map(updated);
        }


        // 3️⃣ OIDB kullanıcı reddi
        if (!valid) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.OIDB_REJECTED,
                    "OIDB manually rejected application"
            );

            return applicationMapper.map(updated);
        }

        // 4️⃣ Kriterler OK → YDYO’ya gönder
        app.setValidationStatus(ValidationStatus.VALID);

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.OIDB_VALIDATED,
                "OIDB validated application after YDYO approval and sent to faculty"
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


        ev.setDecision(decision);
        ev.setYgkMemberId(SecurityUtil.getCurrentUserId());
        evaluationService.save(ev);

        ApplicationStatus target =
                decision == Decision.REJECTED
                        ? ApplicationStatus.YGK_REJECTED
                        : decision == Decision.WAITLISTED
                        ? ApplicationStatus.WAITLISTED
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
                        OidbStatus.OIDB_CRITERIA_REJECTED,
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

    public List<ApplicationResponseDto> getFinalizedResults(Boolean published) {

        List<ApplicationStatus> terminalStatuses = List.of(
                ApplicationStatus.OIDB_REJECTED,
                ApplicationStatus.YGK_APPROVED,
                ApplicationStatus.YGK_REJECTED,
                ApplicationStatus.WAITLISTED
        );

        return applicationRepository
                .findFinalResultsFiltered(terminalStatuses, published)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public List<Application> findFinalResults() {

        List<ApplicationStatus> terminalStatuses = List.of(
                ApplicationStatus.OIDB_REJECTED,
                ApplicationStatus.YGK_APPROVED,
                ApplicationStatus.YGK_REJECTED,
                ApplicationStatus.WAITLISTED
        );

        return applicationRepository
                .findFinalResults(terminalStatuses);
    }

}

