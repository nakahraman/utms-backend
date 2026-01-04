package com.utms.backend.controller;

import com.utms.backend.model.entities.Application;
import com.utms.backend.repository.EnglishPrepService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ydyo")
public class EnglishPrepController {

    private final EnglishPrepService englishPrepService;

    public EnglishPrepController(EnglishPrepService englishPrepService) {
        this.englishPrepService = englishPrepService;
    }

    @PostMapping("/evaluate")
    public Application evaluate(@RequestParam Long appId,
                                @RequestParam boolean passed) {
        return englishPrepService.submitEnglishResult(appId, passed);
    }
}
