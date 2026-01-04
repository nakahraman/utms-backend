package com.utms.backend.repository;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;
import org.springframework.stereotype.Service;

@Service
public class EnglishPrepService {

    private final ApplicationRepository applicationRepository;

    public EnglishPrepService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application submitEnglishResult(Long appId, boolean passed) {

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404","Başvuru bulunamadı"));

        if (passed) {
            app.setStatus(ApplicationStatus.YDYO_APPROVED);
            app.setValidationStatus(ValidationStatus.VALID);
        } else {
            app.setStatus(ApplicationStatus.YDYO_FAILED);
            app.setValidationStatus(ValidationStatus.FLAGGED);
        }

        return applicationRepository.save(app);
    }
}
