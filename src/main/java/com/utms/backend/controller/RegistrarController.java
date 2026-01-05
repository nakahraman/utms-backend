package com.utms.backend.controller;

import com.utms.backend.model.entities.Application;
import com.utms.backend.service.RegistrarService;
import com.utms.backend.service.ResultPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/registrar")
@Tag(name = "Registrar", description = "Registrar office APIs")
public class RegistrarController {

    private final RegistrarService registrarService;
    private final ResultPublishService resultPublishService;

    public RegistrarController(RegistrarService registrarService,
                               ResultPublishService resultPublishService) {
        this.registrarService = registrarService;
        this.resultPublishService = resultPublishService;
    }

    @PreAuthorize("hasRole('REGISTRAR')")
    @GetMapping("/faculty-approved")
    @Operation(summary = "Get faculty approved", description = "Registrar views faculty approved applications")
    public List<Application> getFacultyApproved() {
        return registrarService.getFacultyApprovedApplications();
    }

    @PreAuthorize("hasRole('REGISTRAR')")
    @PostMapping("/receive")
    @Operation(summary = "Receive application", description = "Registrar receives application from faculty")
    public Application receive(@RequestParam Long appId) {
        return registrarService.receiveFromFaculty(appId);
    }

    @PreAuthorize("hasRole('REGISTRAR')")
    @PostMapping("/publish-results")
    @Operation(summary = "Publish results", description = "Registrar publishes final results")
    public String publishResults() {
        resultPublishService.publishResults();
        return "Results published successfully";
    }
}
