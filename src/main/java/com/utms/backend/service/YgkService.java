package com.utms.backend.service;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YgkService {

    private final ApplicationService applicationService;


    public List<ApplicationResponseDto> getInbox() {
        return applicationService.getFacEvaluatedApplicationsForYgk(ApplicationStatus.SENT_TO_YGK);

    }

    public void finalizeApplication(Long appId, Decision decision) {

        applicationService.finalizeApplication(appId, decision);
    }
}
