package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.OidbStatus;
import com.utms.backend.service.ApplicationService;
import com.utms.backend.service.OidbService;
import com.utms.backend.service.ResultPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/oidb")
@Tag(name = "Oidb", description = "Oidb office APIs")
@AllArgsConstructor
public class OidbController {

    private final OidbService oidbService;
    private final ResultPublishService resultPublishService;
    private ApplicationService applicationService;


    @PreAuthorize("hasRole('OIDB')")
    @GetMapping("/inbox")
    @Operation(summary = "Get OIDB inbox with optional status filter", description = "Oidb views submitted applications")
    public List<ApplicationResponseDto> getInbox(
            @RequestParam(required = false) List<OidbStatus> statuses
    ) {
        return oidbService.getInbox(statuses);
    }
    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/send-to-faculty")
    @Operation(summary = "Validate application", description = "Oidb validates application")
    public ApplicationResponseDto validate(@RequestParam Long appId) {
        return oidbService.oidbValidateApplication(appId);
    }

    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/send-to-ygk")
    @Operation(summary = "Send faculty evaluated to YGK",
            description = "OIDB forwards faculty evaluated application to YGK")
    public ApplicationResponseDto sendToYgk(@RequestParam Long appId) {
        return oidbService.sendFacultyEvaluatedToYgk(appId);
    }

    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/publish-results")
    public void publishResults() {
        resultPublishService.publishResults();
    }

    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/send-to-ydyo")
    @Operation(summary = "Send to YDYO",
            description = "OIDB forwards application to YDYO for language assessment")
    public ApplicationResponseDto sendToYdyo(@RequestParam Long appId) {
        return oidbService.sendToYdyo(appId);
    }

    @PreAuthorize("hasRole('OIDB')")
    @PostMapping("/resend-to-faculty")
    public ApplicationResponseDto resendToFaculty(@RequestParam Long appId) {
        return oidbService.resendToFaculty(appId);
    }

    @PreAuthorize("hasRole('OIDB')")
    @GetMapping("/results")
    @Operation(summary = "View finalized and/or published application results")
    public List<ApplicationResponseDto> getFinalizedResults(
            @RequestParam(required = false) Boolean published) {
        return oidbService.getFinalizedResults(published);
    }

}
