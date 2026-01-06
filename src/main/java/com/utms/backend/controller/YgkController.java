package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.service.YgkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ygk")
@Tag(name = "YGK", description = "Higher Education Committee APIs")
public class YgkController {

    private final YgkService ygkService;

    public YgkController(YgkService ygkService) {
        this.ygkService = ygkService;
    }

    @PreAuthorize("hasRole('YGK')")
    @GetMapping("/inbox")
    @Operation(summary = "YGK inbox",
            description = "Returns applications validated by OIDB and approved by faculty")
    public List<ApplicationResponseDto> getInbox() {
        return ygkService.getInbox();
    }

    @PreAuthorize("hasRole('YGK')")
    @PostMapping("/finalize")
    @Operation(summary = "Finalize application",
            description = "YGK gives final decision for application")
    public void finalizeApplication(@RequestParam Long appId,
                                    @RequestParam Decision decision) {
        ygkService.finalizeApplication(appId, decision);
    }
}
