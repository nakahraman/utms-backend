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

        List<Application> apps =
                applicationService.findFinalResults();

        if (apps.isEmpty()) {
            throw new BusinessException("RES-404", "Yayınlanacak sonuç bulunamadı.");
        }

        for (Application app : apps) {

            if (app.isPublished()) continue;

            app.setPublished(true);

            String message =
                    app.getStatus() == ApplicationStatus.OIDB_REJECTED ? "Başvurunuz OIBD tarafından reddedilmiştir." :
                            app.getStatus() == ApplicationStatus.YGK_APPROVED ? "Başvurunuz kabul edilmiştir (Asıl)." :
                                    app.getStatus() == ApplicationStatus.WAITLISTED ? "Başvurunuz yedek listesine alınmıştır." :
                                            "Başvurunuz YGK tarafından reddedilmiştir.";

            notificationService.create(app, NotificationType.RESULT.toString(), message);
        }
    }
}