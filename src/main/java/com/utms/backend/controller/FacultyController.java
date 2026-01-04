package com.utms.backend.controller;

import com.utms.backend.model.entities.Application;
import com.utms.backend.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/evaluated")
    @Operation(summary = "Get evaluated applications", description = "Get department evaluated applications")
    public List<Application> getEvaluated() {
        return facultyService.getDeptEvaluatedApplications();
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve application", description = "Faculty board approves application")
    public Application approve(@RequestParam Long appId) {
        return facultyService.approveFacultyDecision(appId);
    }
}