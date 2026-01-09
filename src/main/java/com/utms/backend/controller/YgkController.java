package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.service.EvaluationRankingService;
import com.utms.backend.service.YgkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ygk")
@Tag(name = "YGK", description = "Higher Education Committee APIs")
@AllArgsConstructor
public class YgkController {

    private final YgkService ygkService;
    private final EvaluationRankingService rankingService;


    @PreAuthorize("hasRole('YGK')")
    @GetMapping("/inbox")
    @Operation(summary = "YGK inbox",
            description = "Returns applications validated by OIDB and approved by faculty")
    public List<ApplicationResponseDto> getInbox() {
        return ygkService.getYgkInbox();
    }


    @PreAuthorize("hasRole('YGK')")
    @PostMapping("/finalize-department")
    @Operation(summary = "Finalize department",
            description = "YGK gives final decision for departments")
    public void finalizeDepartment(@RequestParam Long deptId) {
        rankingService.recalculateRanksAndDecisions(deptId);
    }

    /*
    @PreAuthorize("hasRole('YGK')")
    @PostMapping("/finalize")
    @Operation(summary = "Finalize application",
            description = "YGK gives final decision for application")
    public void finalizeApplication(@RequestParam Long appId,
                                    @RequestParam Decision decision) {
        ygkService.finalizeApplication(appId, decision);
    }

     */


}
