package com.utms.backend.repository;

import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;
import com.utms.backend.service.ApplicationService;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnglishPrepService {

    private final ApplicationService applicationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;

    public ApplicationResponseDto submitEnglishResult(Long appId, boolean passed) {

        Application app = applicationService.findApplicationById(appId);

        if (passed) {
            app.setValidationStatus(ValidationStatus.VALID);
            app = transitionService.transition(app, ApplicationStatus.YDYO_APPROVED,
                    "YDYO approved English proficiency");
        } else {
            app.setValidationStatus(ValidationStatus.FLAGGED);
            app = transitionService.transition(app, ApplicationStatus.YDYO_FAILED,
                    "YDYO failed English proficiency");
        }
        return applicationMapper.map(app);
    }

}
