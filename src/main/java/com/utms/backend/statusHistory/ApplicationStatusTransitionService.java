package com.utms.backend.statusHistory;



import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApplicationStatusTransitionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final CurrentUserService currentUserService;

    public ApplicationStatusTransitionService(ApplicationRepository applicationRepository,
                                              ApplicationStatusHistoryRepository historyRepository,
                                              CurrentUserService currentUserService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.currentUserService = currentUserService;
    }

    public Application transition(Application app, ApplicationStatus toStatus, String reason) {
        ApplicationStatus fromStatus = app.getStatus();

        // Aynı status'e tekrar yazma (isteğe bağlı)
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
