package com.utms.backend.service;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultPublishService {

    private final ApplicationService applicationService;
    private final EvaluationService evaluationService;
    private final NotificationService notificationService;


    public ResultPublishService(ApplicationService applicationService,
                                EvaluationService evaluationService,
                                NotificationService notificationService) {
        this.applicationService = applicationService;
        this.evaluationService = evaluationService;
        this.notificationService = notificationService;
    }

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