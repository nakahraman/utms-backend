package com.utms.backend.service;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YgkService {

    private final ApplicationService applicationService;

    public List<ApplicationResponseDto> getYgkInbox() {
        return applicationService.getFacEvaluatedApplicationsForYgk(ApplicationStatus.SENT_TO_YGK);

    }

}
