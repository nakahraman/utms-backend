package com.utms.backend.statusHistory;



import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                            Set.of(ApplicationStatus.SUBMITTED,
                                    ApplicationStatus.CRITERIA_REJECTED)),

                    // --- OIDB VALIDATION ---
                    Map.entry(ApplicationStatus.SUBMITTED,
                            Set.of(ApplicationStatus.SENT_TO_YDYO)),


                    Map.entry(ApplicationStatus.SENT_TO_YDYO,
                            Set.of(ApplicationStatus.YDYO_APPROVED,
                                    ApplicationStatus.YDYO_EXAM_REQUIRED)),


                    Map.entry(ApplicationStatus.YDYO_EXAM_REQUIRED,
                            Set.of(ApplicationStatus.YDYO_APPROVED,
                                    ApplicationStatus.YDYO_FAILED)),

                    Map.entry(ApplicationStatus.YDYO_APPROVED,
                            Set.of(ApplicationStatus.OIDB_VALIDATED,
                                    ApplicationStatus.OIDB_REJECTED)),

                    Map.entry(ApplicationStatus.CRITERIA_REJECTED,
                            Set.of(ApplicationStatus.OIDB_REJECTED)),

                    Map.entry(ApplicationStatus.YDYO_FAILED,
                            Set.of(ApplicationStatus.OIDB_REJECTED)),

                    // --- FACULTY ---
                    Map.entry(ApplicationStatus.OIDB_VALIDATED,
                            Set.of(ApplicationStatus.FACULTY_EVALUATED,
                                    ApplicationStatus.FACULTY_RETURNED)),


                    Map.entry(ApplicationStatus.FACULTY_EVALUATED,
                            Set.of(ApplicationStatus.SENT_TO_YGK)),


                    // --- RETURN FLOW ---
                    Map.entry(ApplicationStatus.FACULTY_RETURNED,
                            Set.of(ApplicationStatus.OIDB_VALIDATED)),


                    // --- YGK ---
                    Map.entry(ApplicationStatus.SENT_TO_YGK,
                            Set.of(ApplicationStatus.YGK_APPROVED,
                                    ApplicationStatus.WAITLISTED,
                                    ApplicationStatus.YGK_REJECTED)),


                    // --- TERMINAL ---
                    Map.entry(ApplicationStatus.OIDB_REJECTED, Set.of()),
                    Map.entry(ApplicationStatus.YGK_APPROVED, Set.of()),
                    Map.entry(ApplicationStatus.WAITLISTED, Set.of()),
                    Map.entry(ApplicationStatus.YGK_REJECTED, Set.of())

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
}
