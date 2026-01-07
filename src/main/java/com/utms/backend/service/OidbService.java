package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.OidbStatus;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
public class OidbService {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final ApplicationStatusTransitionService transitionService;

    public List<ApplicationResponseDto> getInbox(List<OidbStatus> statuses) {
        return applicationService.getOidbInbox(statuses);
    }

    public ApplicationResponseDto oidbValidateApplication(Long appId, boolean valid) {
        return applicationService.oidbValidateApplication(appId, valid);
    }

    @Transactional
    public ApplicationResponseDto sendFacultyEvaluatedToYgk(Long appId) {

        Application app = applicationService.findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.FACULTY_EVALUATED)
            throw new BusinessException("OIDB-401",
                    "Only faculty evaluated apps can go to YGK");

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.SENT_TO_YGK,
                "OIDB forwarded faculty evaluated application to YGK"
        );

        return applicationMapper.map(updated);
    }

    @Transactional
    public ApplicationResponseDto sendToYdyo(Long appId) {

        Application app = applicationService.findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.SUBMITTED)
            throw new BusinessException("OIDB-402",
                    "Only submitted applications can be sent to YDYO");

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.SENT_TO_YDYO,
                "OIDB forwarded application to YDYO"
        );

        return applicationMapper.map(updated);
    }

    @Transactional
    public ApplicationResponseDto resendToFaculty(Long appId) {

        Application app = applicationService.findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.FACULTY_RETURNED)
            throw new BusinessException("OIDB-406",
                    "Only faculty returned apps can be resent");

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.OIDB_VALIDATED,
                "OIDB resent returned application to faculty"
        );

        return applicationMapper.map(updated);
    }


    public List<ApplicationResponseDto> getFinalizedResults(Boolean published) {

        return applicationService.getFinalizedResults(published);
    }
}
