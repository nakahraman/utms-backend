package com.utms.backend.service;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.statusHistory.ApplicationStatusTransitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RegistrarService {

    private final ApplicationService applicationService;
    private final ApplicationStatusTransitionService transitionService;

    public List<Application> getFacultyApprovedApplications() {
        Long facultyId = SecurityUtil.getCurrentUserFacultyId();
        return applicationService.getDeptEvaluatedApplications(ApplicationStatus.FACULTY_APPROVED, facultyId);
    }

    public Application receiveFromFaculty(Long appId) {

        Application app = applicationService.findById(appId);

        return transitionService.transition(
                app,
                ApplicationStatus.SENT_TO_REGISTRAR,
                "Application received from faculty by registrar"
        );
    }
}
