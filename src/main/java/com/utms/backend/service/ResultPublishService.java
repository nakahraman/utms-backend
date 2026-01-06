package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.model.enums.NotificationType;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ResultPublishService {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;

    /**
     * UC-006 – Publish Final Results
     * Actor: ÖİDB
     */
    @Transactional
    public void publishResults() {

        List<Application> apps = applicationService.findBYStatusIn(List.of(
                ApplicationStatus.YGK_APPROVED,
                ApplicationStatus.YGK_REJECTED));

        if (apps.isEmpty()) {
            throw new BusinessException("RES-404", "Yayınlanacak sonuç bulunamadı.");
        }

        for (Application app : apps) {

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.RESULT_PUBLISHED,
                    "Final results published by OIDB"
            );

            String message =
                    updated.getDecision() == Decision.REJECTED ? "Başvurunuz reddedilmiştir." :
                            updated.getDecision() == Decision.PRIMARY ? "Başvurunuz kabul edilmiştir (Asıl)." :
                                    "Başvurunuz yedek listesine alınmıştır.";

            notificationService.create(updated, NotificationType.RESULT.toString(), message);
        }
    }
}