package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationSubmitRequest;
import com.utms.backend.model.entities.Application;
import com.utms.backend.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/applications")
@Tag(name = "Applications", description = "Application management APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit application", description = "Submit a new transfer application")
    public Application submit(@RequestBody ApplicationSubmitRequest request) {
        return applicationService.submitApplication(
                request.studentId(), request.departmentId()
        );
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get applications by student", description = "Retrieve all applications for a specific student")
    public List<Application> getApplicationsByStudent(@PathVariable Long studentId) {
        return applicationService.getApplicationsByStudent(studentId);
    }

    @GetMapping("/oidb/submitted")
    @Operation(summary = "Get submitted applications", description = "Get all submitted applications for OIDB")
    public List<Application> getSubmittedApplications() {
        return applicationService.getSubmittedApplications();
    }

    @PostMapping("/oidb/validate")
    @Operation(summary = "Validate application", description = "Validate or reject an application")
    public Application validateApplication(@RequestParam Long appId,
                                           @RequestParam boolean valid) {
        return applicationService.validateApplication(appId, valid);
    }

    @GetMapping("/faculty/inbox")
    @Operation(summary = "Get faculty inbox", description = "Get validated applications for faculty review")
    public List<Application> getFacultyInbox() {
        return applicationService.getValidatedApplicationsForFaculty();
    }

    @PostMapping("/faculty/send-to-department")
    @Operation(summary = "Send to department", description = "Forward application to department")
    public Application sendToDepartment(@RequestParam Long appId) {
        return applicationService.sendToDepartment(appId);
    }
}