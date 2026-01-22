package com.utms.backend.statusHistory;



import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.service.ApplicationService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
public class ApplicationStatusTransitionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final CurrentUserService currentUserService;

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> RULES =
            Map.ofEntries(

                    // --- SUBMIT STAGE ---
                    Map.entry(ApplicationStatus.DRAFT,
                            Set.of(ApplicationStatus.SUBMITTED)),

                    // --- OIDB → YDYO ---
                    Map.entry(ApplicationStatus.SUBMITTED,
                            Set.of(ApplicationStatus.SENT_TO_YDYO)),

                    // --- YDYO ---
                    Map.entry(ApplicationStatus.SENT_TO_YDYO,
                            Set.of(ApplicationStatus.YDYO_APPROVED,
                                    ApplicationStatus.YDYO_EXAM_REQUIRED)),

                    Map.entry(ApplicationStatus.YDYO_EXAM_REQUIRED,
                            Set.of(ApplicationStatus.YDYO_APPROVED,
                                    ApplicationStatus.YDYO_FAILED)),

                    Map.entry(ApplicationStatus.YDYO_APPROVED,
                            Set.of(ApplicationStatus.OIDB_VALIDATED,
                                    ApplicationStatus.OIDB_FLAGGED)),

                    Map.entry(ApplicationStatus.YDYO_FAILED,
                            Set.of(ApplicationStatus.YDYO_CRITERIA_NOT_MET)),

                    // --- OIDB ---
                    Map.entry(ApplicationStatus.OIDB_FLAGGED,
                            Set.of(ApplicationStatus.OIDB_RETURNED,
                                    ApplicationStatus.OIDB_VALIDATED)),

                    Map.entry(ApplicationStatus.OIDB_RETURNED,
                            Set.of(ApplicationStatus.SUBMITTED)),

                    // --- FACULTY ---
                    Map.entry(ApplicationStatus.OIDB_VALIDATED,
                            Set.of(ApplicationStatus.FACULTY_EVALUATED,
                                    ApplicationStatus.FACULTY_RETURNED)),

                    Map.entry(ApplicationStatus.FACULTY_RETURNED,
                            Set.of(ApplicationStatus.OIDB_VALIDATED)),

                    Map.entry(ApplicationStatus.FACULTY_EVALUATED,
                            Set.of(ApplicationStatus.SENT_TO_YGK)),


                    // --- YGK ---
                    Map.entry(ApplicationStatus.SENT_TO_YGK,
                            Set.of(ApplicationStatus.YGK_APPROVED,
                                    ApplicationStatus.YGK_WAITLISTED,
                                    ApplicationStatus.YGK_REJECTED,
                                    ApplicationStatus.ACADEMICALLY_INELIGIBLE)),

                    // --- OIDB PUBLISH RESULTS ---
                    Map.entry(ApplicationStatus.YGK_APPROVED,
                            Set.of(ApplicationStatus.RESULT_PUBLISHED)),
                    Map.entry(ApplicationStatus.YGK_WAITLISTED,
                            Set.of(ApplicationStatus.RESULT_PUBLISHED)),
                    Map.entry(ApplicationStatus.YGK_REJECTED,
                            Set.of(ApplicationStatus.RESULT_PUBLISHED)),
                    Map.entry(ApplicationStatus.ACADEMICALLY_INELIGIBLE,
                            Set.of(ApplicationStatus.RESULT_PUBLISHED)),

                    // --- TERMINAL ---
                    Map.entry(ApplicationStatus.RESULT_PUBLISHED, Set.of())
            );

    @Transactional
    public Application transition(Application app, ApplicationStatus toStatus, String reason) {

        ApplicationStatus fromStatus = app.getStatus();

        // ❌ Kural dışı geçiş
        if (!RULES.containsKey(fromStatus) || !RULES.get(fromStatus).contains(toStatus)) {
            throw new BusinessException(
                    "STAT-400",
                    "Geçersiz durum geçişi: " + fromStatus + " → " + toStatus
            );
        }

        // Aynı status'e tekrar yazma
        if (fromStatus == toStatus) {
            return app;
        }

        // 1) App status güncelle
        app.setStatus(toStatus);
        app.setSubmissionDate(LocalDateTime.now());
        Application saved = applicationRepository.save(app);

        // 2) History yaz
        ApplicationStatusHistory h = ApplicationStatusHistory.builder()
                .application(saved)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(currentUserService.usernameOrSystem())
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();

        historyRepository.save(h);

        return saved;
    }

    public List<ApplicationStatusHistory> findApplicationByCharged(Long appId) {
        return historyRepository.findByApplication_AppIdOrderByChangedAtDesc(appId);
    }
}
