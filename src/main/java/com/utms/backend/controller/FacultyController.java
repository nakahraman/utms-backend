package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/faculty")
@Tag(name = "Faculty", description = "Faculty board decision APIs")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/evaluated")
    @Operation(summary = "Get evaluated applications", description = "Faculty views department evaluated applications")
    public List<ApplicationResponseDto> getEvaluated() {
        return facultyService.getDeptEvaluatedApplications();
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PostMapping("/approve")
    @Operation(summary = "Approve application", description = "Faculty board approves evaluated application")
    public ApplicationResponseDto approve(@RequestParam Long appId) {
        return facultyService.approveFacultyDecision(appId);
    }
}
