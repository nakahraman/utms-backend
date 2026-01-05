package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.model.entities.Application;
import com.utms.backend.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/applications")
@Tag(name = "Applications", description = "Student application operations")
@AllArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/submit")
    @Operation(summary = "Submit application", description = "Student submits new transfer application")
    public ApplicationResponseDto submit(@RequestBody ApplicationSubmitRequest request) {
        return applicationService.submitApplication(request.studentId(), request.departmentId());
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get own applications", description = "Student views own applications")
    public List<ApplicationResponseDto> getApplicationsByStudent(@PathVariable Long studentId) {
        return applicationService.getApplicationsByStudent(studentId);
    }

    @PreAuthorize("hasRole('OIDB')")
    @GetMapping("/oidb/submitted")
    @Operation(summary = "Get submitted applications", description = "Registrar gets submitted applications")
    public List<ApplicationResponseDto> getSubmittedApplications() {
        return applicationService.getSubmittedApplications();
    }

    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/oidb/validate")
    @Operation(summary = "Validate application", description = "Registrar validates application")
    public ApplicationResponseDto validateApplication(@RequestParam Long appId,
                                           @RequestParam boolean valid) {
        return applicationService.validateApplication(appId, valid);
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/faculty/inbox")
    @Operation(summary = "Faculty inbox", description = "Faculty views validated applications")
    public List<ApplicationResponseDto> getFacultyInbox() {
        return applicationService.getValidatedApplicationsForFaculty();
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PostMapping("/faculty/send-to-department")
    @Operation(summary = "Send to department", description = "Faculty forwards application to department")
    public ApplicationResponseDto sendToDepartment(@RequestParam Long appId) {
        return applicationService.sendToDepartment(appId);
    }
}
