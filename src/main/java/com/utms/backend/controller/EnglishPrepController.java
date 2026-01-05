package com.utms.backend.controller;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.repository.EnglishPrepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ydyo")
@Tag(name = "YDYO", description = "English proficiency evaluation APIs")
public class EnglishPrepController {

    private final EnglishPrepService englishPrepService;

    public EnglishPrepController(EnglishPrepService englishPrepService) {
        this.englishPrepService = englishPrepService;
    }

    @PreAuthorize("hasRole('YDYO')")
    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate English proficiency", description = "YDYO evaluates student's English result")
    public ApplicationResponseDto evaluate(@RequestParam Long appId,
                                           @RequestParam boolean passed) {
        return englishPrepService.submitEnglishResult(appId, passed);
    }
}
