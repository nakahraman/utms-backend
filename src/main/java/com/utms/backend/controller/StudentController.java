package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.record.ApplicationSubmitRequest;
import com.utms.backend.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.utms.backend.security.SecurityUtil;

import java.util.List;

@RestController
@RequestMapping("/student")
@Tag(name = "Student", description = "Student APIs")
@AllArgsConstructor
public class StudentController {

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

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/results")
    public ApplicationResponseDto getMyResult() {
        return applicationService.getMyResult();
    }
}
