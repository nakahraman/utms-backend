package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.ApplicationStatusHistoryDto;
import com.utms.backend.model.dto.StudentProfileDto;
import com.utms.backend.security.SecurityUtil;
import com.utms.backend.service.ApplicationService;
import com.utms.backend.statusHistory.ApplicationStatusHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/student")
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
    @Operation(summary = "Get own applications", description = "Student views own applications")
    @GetMapping("/me/applications")
    public List<ApplicationResponseDto> getMyApplications() {

        Long studentId = SecurityUtil.getCurrentStudentId();
        return applicationService.getApplicationsByStudent(studentId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get own applications", description = "Student views own latest applications")
    @GetMapping("/me/applications/latest")
    public ApplicationResponseDto getMyLatestApplication() {
        return applicationService.getLatestMyApplication();
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/results")
    public ApplicationResponseDto getMyResult() {
        return applicationService.getMyResult();
    }


    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public StudentProfileDto getMyProfile() {

        Long studentId = SecurityUtil.getCurrentStudentId();
        return applicationService.getMyStudentProfile(studentId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me/{id}")
    public ApplicationResponseDto getMyApplication(@PathVariable Long id) {

        Long studentId = SecurityUtil.getCurrentStudentId();
        return applicationService.getMyApplicationById(studentId, id);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me/applications/{appId}/history")
    public List<ApplicationStatusHistoryDto> getMyApplicationHistory(@PathVariable Long appId) {
        return applicationService.getMyApplicationHistory(appId);
    }

}
