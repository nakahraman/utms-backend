package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ResultPublishService {

    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final ApplicationStatusTransitionService transitionService;

    /**
     * UC-006 – Publish Final Results
     * Actor: ÖİDB
     */
    @Transactional
    public void publishResults() {

        List<Application> apps = applicationService.findPublishableResults();

        if (apps.isEmpty())
            throw new BusinessException("RES-404","Yayınlanacak sonuç bulunamadı.");

        for (Application app : apps) {

            if (app.isPublished()) continue;

            if (!facultyDataComplete(app)) {

                transitionService.transition(
                        app,
                        ApplicationStatus.OIDB_FLAGGED,
                        "Faculty evaluation data missing – cannot publish"
                );
                continue;
            }

            ApplicationStatus finalStatus = mapFinalStatus(app);

            Application updated = transitionService.transition(
                    app,
                    ApplicationStatus.RESULT_PUBLISHED,
                    "Final result published by OIDB"
            );

            updated.setPublished(true);

            try {
                notificationService.create(
                        updated,
                        finalStatus,
                        buildMessage(finalStatus)
                );
            } catch (Exception ex) {
                System.out.println("EMAIL-FAIL appId=" + app.getAppId());
            }
        }
    }

    private ApplicationStatus mapFinalStatus(Application app) {

        return switch (app.getStatus()) {
            case YGK_APPROVED -> ApplicationStatus.YGK_APPROVED;
            case YGK_WAITLISTED -> ApplicationStatus.YGK_WAITLISTED;
            case YGK_REJECTED, ACADEMICALLY_INELIGIBLE -> ApplicationStatus.YGK_REJECTED;
            default -> throw new BusinessException("RES-500",
                    "Incorrect status mapping for publish");
        };
    }

    private String buildMessage(ApplicationStatus status) {
        return switch (status) {
            case YGK_APPROVED -> "Başvurunuz ASİL olarak kabul edilmiştir.";
            case YGK_WAITLISTED -> "Başvurunuz YEDEK listeye alınmıştır.";
            case YGK_REJECTED -> "Başvurunuz REDDEDİLMİŞTİR.";
            default -> "Başvurunuz sonuçlandırılmıştır.";
        };
    }

    private boolean facultyDataComplete(Application app) {

        if (app.getEvaluations() == null || app.getEvaluations().isEmpty())
            return false;

        return app.getEvaluations()
                .stream()
                .anyMatch(e -> e.getDecision() != null);
    }


}