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
import com.utms.backend.repository.StudentRepository;
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
    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final ExternalVerificationClient externalClient;
    private final TransferDocumentService documentService;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;
    private final ExternalAcademicSnapshotClient academicSnapshotClient;
    private final AcademicEligibilityEvaluator eligibilityEvaluator;
    private final ExternalEligibilityExtractor externalEligibilityExtractor;
    private final EnglishScoreService englishScoreService;
    private final EvaluationService evaluationService;
    private final StudentService studentService;
    private final UserService userService;

    private final List<ApplicationStatus> ALLOWED_FOR_NEW_APPLICATION = List.of(ApplicationStatus.DRAFT);


    // ---------- DRAFT ----------

    @Transactional
    public Long createExternalDraft(Long userId, Long departmentId) {

        Student student = studentService.getOrCreateStudent(userId);
        Department dept = departmentService.findDepartmentById(departmentId);

        Application app = createDraft(student, dept);
        return app.getAppId();
    }

    private Application createDraft(Student student, Department department) {

        Application app = new Application();
        app.setStudent(student);
        app.setDepartment(department);
        app.setSubmissionDate(LocalDateTime.now());
        app.setStatus(ApplicationStatus.DRAFT);
        return applicationRepository.save(app);
    }


    // ---------- EXTERNAL SUBMIT ----------

    @Transactional
    public ApplicationResponseDto submitExternalApplication(Long appId) {

        Application app = authorizeAndLoadDraft(appId);

        checkCanSubmit(app);

        documentService.validateMandatoryDocuments(app);

        AcademicEligibilitySnapshot snapshot = externalEligibilityExtractor.extract(app);

        validateEligibilityOrReject(app, snapshot);

        finalizeSubmission(app);

        sendSubmitNotification(app);
        return applicationMapper.map(app);
    }

    private Application authorizeAndLoadDraft(Long appId) {

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
    public ApplicationResponseDto submitInternalApplication(Long userId, Long departmentId) {

        Student student = studentService.findStudentIdByUserId(userId)
                .orElseThrow(() ->
                        new BusinessException("STU-404",
                                "Bu kullanıcıya ait öğrenci profili bulunamadı"));;

        Department dept = departmentService.findDepartmentById(departmentId);

        checkCanSubmit(student.getStudentId(), dept.getDeptId());

        Application app = createDraft(student, dept);

        AcademicEligibilitySnapshot snapshot = academicSnapshotClient.fetchSnapshot(student.getStudentId().toString(), dept.getDeptId());

        if (!eligibilityEvaluator.isEligible(snapshot, dept.getCriteria().toDto())) {
            transitionService.transition(app, ApplicationStatus.CRITERIA_REJECTED, "Internal academic criteria not met");
        } else {
            transitionService.transition(app, ApplicationStatus.SUBMITTED, "Internal academic eligibility passed");
        }

        sendSubmitNotification(app);
        return applicationMapper.map(app);
    }

    // ---------- DOMAIN RULES ----------

    private void checkCanSubmit(Application app) {
        checkCanSubmit(app.getStudent().getStudentId(), app.getDepartment().getDeptId());
    }

    private void checkCanSubmit(Long studentId, Long deptId) {

        if (applicationRepository.existsByStudent_StudentIdAndDepartment_DeptIdAndStatusNotIn(studentId, deptId, ALLOWED_FOR_NEW_APPLICATION)) {

            throw new BusinessException("APP-409", "Bu bölüm için devam eden veya sonuçlanmış bir başvurunuz bulunmaktadır.");
        }
    }

    private void validateEligibilityOrReject(Application app, AcademicEligibilitySnapshot snapshot) {

        DepartmentCriteriaDto criteria = app.getDepartment().getCriteria().toDto();

        if (!eligibilityEvaluator.isEligible(snapshot, criteria)) {

            transitionService.transition(app, ApplicationStatus.CRITERIA_REJECTED, "External academic criteria not met");

            throw new BusinessException("ELIG-EXT-001", "Bölüm kriterleri sağlanamadı");
        }
    }

    private void finalizeSubmission(Application app) {

        transitionService.transition(app, ApplicationStatus.SUBMITTED, "External application submitted");
    }

    private void sendSubmitNotification(Application app) {

        notificationService.create(app, "SUBMIT", "Yatay geçiş başvurunuz başarıyla alınmıştır. Başvuru No: " + app.getAppId());
    }

    @Transactional
    private Application createDraftApplication(Student student, Department department) {

        checkDuplicate(student.getStudentId(), department.getDeptId());

        Application app = new Application();
        app.setStudent(student);
        app.setDepartment(department);
        app.setGpa(student.getGpa());
        app.setSubmissionDate(LocalDateTime.now());
        app.setStatus(ApplicationStatus.DRAFT);

        return applicationRepository.save(app);
    }

    @Transactional
    private void handleInternalStudentFlow(Application app, Department department) {

        AcademicEligibilitySnapshot snapshot =
                academicSnapshotClient.fetchSnapshot(
                        app.getStudent().getStudentId().toString(),
                        department.getDeptId()
                );

        DepartmentCriteriaDto criteria = department.getCriteria().toDto();

        if (!eligibilityEvaluator.isEligible(snapshot, criteria)) {

            transitionService.transition(app, ApplicationStatus.CRITERIA_REJECTED,
                    "Internal academic criteria not met");
        } else {
            transitionService.transition(app, ApplicationStatus.SUBMITTED,
                    "Internal academic eligibility passed");
        }
    }



    private Department findDepartment(Long id) {
        return departmentService.findDepartmentById(id);
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

        // ❌ Akademik kriter reddi
        if (app.getStatus() == ApplicationStatus.CRITERIA_REJECTED) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            return applicationMapper.map(
                    transitionService.transition(
                            app,
                            ApplicationStatus.OIDB_REJECTED,
                            "Application rejected by OIDB due to academic ineligibility"
                    )
            );
        }

        // ❌ YDYO'dan kalan otomatik reddedilir
        if (app.getStatus() == ApplicationStatus.YDYO_FAILED) {

            app.setValidationStatus(ValidationStatus.FLAGGED);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.OIDB_REJECTED,
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
                    ApplicationStatus.OIDB_REJECTED,
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
                        OidbStatus.CRITERIA_REJECTED,
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

