package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/faculty")
@Tag(name = "Faculty", description = "Faculty academic evaluation APIs")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/inbox")
    @Operation(summary = "Faculty inbox", description = "Faculty views validated applications")
    public List<ApplicationResponseDto> getFacultyInbox() {
        return facultyService.getFacultyInbox();
    }


    @PreAuthorize("hasRole('FACULTY')")
    @PostMapping("/evaluate")
    @Operation(summary = "Send to YGK(department)", description = "Faculty forwards application to YGK(department)")
    public List<EvaluationResponseDto> evaluate(@RequestParam int quota) {
        return facultyService.evaluateFacultyApplications(quota);
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PostMapping("/return")
    public ApplicationResponseDto returnToOidb(@RequestParam Long appId,
                                               @RequestParam String reason) {
        return facultyService.returnToOidb(appId, reason);
    }


    /*
    @PostMapping("/send-to-ygk")
    @Operation(summary = "Send to YGK(department)", description = "Faculty forwards application to YGK(department)")
    public ApplicationResponseDto sendToYGK(@RequestParam Long appId,
                                            @RequestParam boolean valid) {
        return facultyService.evaluateFaculty(appId, valid);
    }

 */
}
