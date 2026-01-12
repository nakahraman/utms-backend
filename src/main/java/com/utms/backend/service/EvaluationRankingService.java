package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Department;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.model.enums.ValidationStatus;
import com.utms.backend.repository.EvaluationRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationRankingService {

    private final EvaluationRepository evaluationRepository;
    private final DepartmentService departmentService;
    private final ApplicationStatusTransitionService transitionService;
    private final YgkAcademicEligibilityService ygkAcademicEligibilityService;


    @Transactional
    public void recalculateRanksAndDecisions(Long deptId) {

        Department dept = departmentService.findDepartmentById(deptId);

        List<Evaluation> evals =
                evaluationRepository.findByDepartmentOrderedByScoreDesc(deptId);

        if (evals.isEmpty())
            throw new BusinessException("YGK-404",
                    "No applications found for this department.");

        // 🔒 DAHA ÖNCE FİNALİZE EDİLMİŞ Mİ?
        boolean alreadyFinalized =
                evals.stream().anyMatch(e ->
                        e.getApplication().getStatus() != ApplicationStatus.SENT_TO_YGK);

        if (alreadyFinalized)
            throw new BusinessException("YGK-401",
                    "This department has already been finalized.");

        validateQuota(evals, dept);

        int quota = dept.getQuota();
        int waitlistQuota = dept.getWaitlistQuota();

        Long currentYgkMemberId = SecurityUtil.getCurrentUserId();

        int rank = 1;

        for (Evaluation ev : evals) {

            // SKOR YOKSA FİNALİZE EDİLEMEZ
            if (ev.getScore() == null)
                throw new BusinessException("YGK-402",
                        "Application has no evaluation score.");


            Application app = ev.getApplication();

            //  BURASI AKADEMİK UYGUNLUK KONTROLÜ
            try {
                ygkAcademicEligibilityService.validate(app, dept);

            } catch (BusinessException ex) {

                app.setValidationStatus(ValidationStatus.FLAGGED);
                ev.setDecision(Decision.REJECTED);
                app.setDecision(Decision.REJECTED);
                ev.setYgkMemberId(currentYgkMemberId);

                transitionService.transition(app,
                        ApplicationStatus.ACADEMICALLY_INELIGIBLE,
                        ex.getMessage());
                continue;   // ❗ bu başvuru sıralamaya GİRMEZ
            }

            //  sadece uygun olanlar aşağıya iner
            app.setValidationStatus(ValidationStatus.VALID);
            ev.setRank(rank);
            ev.setYgkMemberId(currentYgkMemberId);

            if (rank <= quota) {

                ev.setDecision(Decision.PRIMARY);
                app.setDecision(Decision.PRIMARY);

                transitionService.transition(
                        app,
                        ApplicationStatus.YGK_APPROVED,
                        "Department ranking completed: Applicant ranked " + rank +
                        " within quota " + quota + ". Marked as PRIMARY."
                );

            } else if (rank <= quota + waitlistQuota) {

                ev.setDecision(Decision.WAITLISTED);
                app.setDecision(Decision.WAITLISTED);

                transitionService.transition(
                        app,
                        ApplicationStatus.YGK_WAITLISTED,
                        "Department ranking completed: Applicant ranked " + rank +
                        ", placed on WAITLIST (quota " + quota +
                        ", waitlist limit " + waitlistQuota + ")."
                );

            } else {

                ev.setDecision(Decision.REJECTED);
                app.setDecision(Decision.REJECTED);

                transitionService.transition(
                        app,
                        ApplicationStatus.YGK_REJECTED,
                        "Department ranking completed: Applicant ranked " + rank +
                        ", exceeds quota and waitlist limits. Marked as REJECTED."
                );
            }
            rank++;
        }
        // 🔒 Tek seferde flush – transaction sonunda Hibernate dirty-checking çalışır
        evaluationRepository.saveAll(evals);
    }

    private void validateQuota(List<Evaluation> evals, Department dept) {

        if (evals.size() > dept.getQuota() + dept.getWaitlistQuota()) {
            return; // normal – reddedilecekler olacaktır
        }
    }

}
