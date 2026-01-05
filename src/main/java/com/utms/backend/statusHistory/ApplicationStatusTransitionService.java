package com.utms.backend.statusHistory;



import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.repository.ApplicationRepository;
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

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> RULES = Map.of(
            ApplicationStatus.VALIDATED, Set.of(ApplicationStatus.SENT_TO_DEPARTMENT),
            ApplicationStatus.SENT_TO_DEPARTMENT, Set.of(ApplicationStatus.DEPT_EVALUATED),
            ApplicationStatus.DEPT_EVALUATED, Set.of(ApplicationStatus.FACULTY_APPROVED),
            ApplicationStatus.FACULTY_APPROVED, Set.of(ApplicationStatus.SENT_TO_REGISTRAR),
            ApplicationStatus.SENT_TO_REGISTRAR, Set.of(
                    ApplicationStatus.APPROVED,
                    ApplicationStatus.WAITLISTED,
                    ApplicationStatus.REJECTED
            )
    );


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
