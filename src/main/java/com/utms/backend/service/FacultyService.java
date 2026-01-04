package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.entities.Application;
import com.utms.backend.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyService {

    private final ApplicationRepository applicationRepository;

    public FacultyService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public List<Application> getDeptEvaluatedApplications() {
        return applicationRepository.findByStatus("DeptEvaluated");
    }

    public Application approveFacultyDecision(Long appId) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        app.setStatus(ApplicationStatus.FACULTY_APPROVED);

        return applicationRepository.save(app);
    }
}
