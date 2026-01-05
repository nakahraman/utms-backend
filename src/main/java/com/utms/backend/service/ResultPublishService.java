package com.utms.backend.service;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.StudentType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ResultPublishService {

    private final ApplicationService applicationService;
    private final EvaluationService evaluationService;
    private final NotificationService notificationService;

    public void publishResults() {

        List<Application> apps = getApplicationsSentToRegistrar();

        for (Application app : apps) {

            if (app.getStatus() == ApplicationStatus.YDYO_FAILED)
                continue;

            Evaluation ev = evaluationService.findApplicaitonByAppId(app.getAppId());
            if (ev == null) continue;

            applicationService.finalizeApplicationResult(app.getAppId(), ev);

            String message = "Başvuru sonucunuz: " + app.getStatus();

            if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {
                message += " (Belgeleriniz manuel olarak değerlendirilmiştir.)";
            }

            notificationService.create(app, "RESULT", message);
        }
    }

    public List<Application> getApplicationsSentToRegistrar() {
        return applicationService.getApplicationsByStatus(ApplicationStatus.SENT_TO_REGISTRAR);
    }
}