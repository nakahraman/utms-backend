package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YgkService {

    private final ApplicationService applicationService;
    private final YgkAcademicEligibilityService ygkEligibilityService;
    private final ApplicationStatusTransitionService transitionService;


    public List<ApplicationResponseDto> getInbox() {
        return applicationService.getFacEvaluatedApplicationsForYgk(ApplicationStatus.SENT_TO_YGK);

    }

    public void finalizeApplication(Long appId, Decision decision) {

        applicationService.finalizeApplication(appId, decision);
    }

    public void evaluateApplication(Application app) {

        try {
            ygkEligibilityService.validate(app, app.getDepartment());
        } catch (BusinessException ex) {

            transitionService.transition(app,
                    ApplicationStatus.ACADEMICALLY_INELIGIBLE,
                    ex.getMessage());
            return;
        }

        // sadece uygun olanlar sıralamaya girer
    }

}
