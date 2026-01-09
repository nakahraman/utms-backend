package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
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

            String message;

            if (app.getStatus() == ApplicationStatus.YGK_APPROVED)
                message = "Başvurunuz ASİL olarak kabul edilmiştir.";
            else if (app.getStatus() == ApplicationStatus.YGK_WAITLISTED)
                message = "Başvurunuz yedek listesine alınmıştır.";
            else if (app.getStatus() == ApplicationStatus.YGK_REJECTED)
                message = "Başvurunuz YGK tarafından reddedilmiştir.";
            else if (app.getStatus() == ApplicationStatus.ACADEMICALLY_INELIGIBLE)
                message = "Başvurunuz akademik kriterleri sağlamadığı için değerlendirmeye alınmamıştır.";
            else
                message = "Başvurunuz sonuçlandırılmıştır.";


            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.RESULT_PUBLISHED,
                    message
            );

            updated.setPublished(true);

            notificationService.create(app, NotificationType.RESULT.toString(), message);
        }
    }
}