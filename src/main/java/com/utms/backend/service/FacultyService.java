package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@AllArgsConstructor
public class FacultyService {

    private final ApplicationService applicationService;
    private final ApplicationStatusTransitionService transitionService;
    private final ApplicationMapper applicationMapper;
    private final EvaluationService evaluationService;


    public List<ApplicationResponseDto> getFacultyInbox() {

        Long facultyId = SecurityUtil.getCurrentUserFacultyId();

        List<ApplicationStatus> statuses =
                List.of(ApplicationStatus.OIDB_VALIDATED);

        return applicationService
                .findByStatusInAndFacultyId(statuses, facultyId);
    }


    @Transactional
    public ApplicationResponseDto evaluateFaculty(Long appId, boolean valid) {

        Application app = applicationService.findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.SENT_TO_YGK)
            throw new BusinessException("YGK-401","Only applications awaiting department evaluation can be forwarded to the YGK.");

        ApplicationStatus nextStatus = valid
                ? ApplicationStatus.SENT_TO_YGK
                : ApplicationStatus.RETURNED_TO_OIDB;

        String reason = valid
                ? "Faculty approved and forwarded to YGK"
                : "Faculty rejected and returned to OIDB";

        Application updated = transitionService.transition(app, nextStatus, reason);

        return applicationMapper.map(updated);
    }


    @Transactional
    public List<EvaluationResponseDto> evaluateFacultyApplications() {

        Long facultyId = SecurityUtil.getCurrentUserFacultyId();

        List<ApplicationStatus> statuses =
                List.of(ApplicationStatus.OIDB_VALIDATED);

        List<Application> apps =
                applicationService.findByStatusInAndFacultyIdForEval(statuses, facultyId);

        // Skorlama + sıralama + FACULTY_EVALUATED
        return evaluationService.evaluateApplications(apps);
    }

    @Transactional
    public ApplicationResponseDto returnToOidb(Long appId, String reason) {

        Application app = applicationService.findApplicationById(appId);

        if (app.getStatus() != ApplicationStatus.OIDB_VALIDATED)
            throw new BusinessException("FAC-401",
                    "Only applications sent to faculty can be returned");

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.FACULTY_RETURNED,
                reason
        );

        return applicationMapper.map(updated);
    }

}