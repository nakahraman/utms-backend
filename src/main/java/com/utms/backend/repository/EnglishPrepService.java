package com.utms.backend.repository;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;
import com.utms.backend.service.ApplicationService;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import org.springframework.stereotype.Service;

@Service
public class EnglishPrepService {

    private final ApplicationService applicationService;
    private final ApplicationStatusTransitionService transitionService;

    public EnglishPrepService(ApplicationService applicationService,
                              ApplicationStatusTransitionService transitionService) {
        this.applicationService = applicationService;
        this.transitionService = transitionService;
    }

    public Application submitEnglishResult(Long appId, boolean passed) {

        Application app = applicationService.findById(appId);

        if (passed) {
            app.setValidationStatus(ValidationStatus.VALID);
            return transitionService.transition(
                    app,
                    ApplicationStatus.YDYO_APPROVED,
                    "YDYO approved English proficiency"
            );
        } else {
            app.setValidationStatus(ValidationStatus.FLAGGED);
            return transitionService.transition(
                    app,
                    ApplicationStatus.YDYO_FAILED,
                    "YDYO failed English proficiency"
            );
        }
    }

}
