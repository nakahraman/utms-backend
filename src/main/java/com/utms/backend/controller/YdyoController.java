package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ydyo")
@RequiredArgsConstructor
@Tag(name = "YDYO", description = "Language assessment APIs")
public class YdyoController {

    private final ApplicationService applicationService;

    @PreAuthorize("hasRole('YDYO')")
    @GetMapping("/inbox")
    public List<ApplicationResponseDto> getInbox() {
        return applicationService.getApplicationsByStatus(ApplicationStatus.SENT_TO_YDYO);
    }

    @PreAuthorize("hasRole('YDYO')")
    @PostMapping("/validate")
    public ApplicationResponseDto validate(@RequestParam Long appId) {
        return applicationService.validateYdyo(appId);
    }

    @PreAuthorize("hasRole('YDYO')")
    @PostMapping("/placement-exam")
    public ApplicationResponseDto submitPlacementExam(@RequestParam Long appId,
                                                      @RequestParam boolean passed) {
        return applicationService.submitPlacementExamResult(appId, passed);
    }
}

