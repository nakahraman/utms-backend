package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@Tag(name = "Student", description = "Student APIs")
@AllArgsConstructor
public class StudentController {

    private final ApplicationService applicationService;


    @PostMapping("/draft")
    @PreAuthorize("hasRole('STUDENT')")
    public Long createDraft(@RequestParam Long departmentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return applicationService.createDraft(currentUserId, departmentId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/ext-submit/{appId}")
    @Operation(summary = "Submit external application", description = "External student submits new transfer application")
    public ApplicationResponseDto submitExternal(@PathVariable Long appId) {
        return applicationService.submitExternalApplication(appId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit/{appId}")
    @Operation(summary = "Submit application", description = "Student submits new transfer application")
    public ApplicationResponseDto submit(@PathVariable Long appId) {

        Long userId = SecurityUtil.getCurrentUserId();
        return applicationService.submitInternalApplication(userId, appId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{studentId}")
    @Operation(summary = "Get own applications", description = "Student views own applications")
    public List<ApplicationResponseDto> getApplicationsByStudent(@PathVariable Long studentId) {
        return applicationService.getApplicationsByStudent(studentId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/results")
    public ApplicationResponseDto getMyResult() {
        return applicationService.getMyResult();
    }
}
