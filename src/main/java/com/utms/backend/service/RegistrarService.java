package com.utms.backend.service;

import com.utms.backend.mapper.ApplicationMapper;
import com.utms.backend.model.dto.ApplicationResponseDto;
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
    private final ApplicationMapper applicationMapper;

    public List<ApplicationResponseDto> getFacultyApprovedApplications() {

        Long facultyId = SecurityUtil.getCurrentUserFacultyId();

        return applicationService
                .getDeptEvaluatedApplications(ApplicationStatus.FACULTY_APPROVED, facultyId)
                .stream()
                .map(applicationMapper::map)
                .toList();
    }

    public ApplicationResponseDto receiveFromFaculty(Long appId) {

        Application app = applicationService.findApplicationById(appId);

        Application updated = transitionService.transition(
                app,
                ApplicationStatus.SENT_TO_REGISTRAR,
                "Application received from faculty by registrar"
        );

        return applicationMapper.map(updated);
    }
}
