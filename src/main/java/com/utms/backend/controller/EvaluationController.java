package com.utms.backend.controller;

import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.service.EvaluationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/ygk")
@Tag(name = "YGK", description = "Department evaluation APIs")
@AllArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PreAuthorize("hasAnyRole('OIDB','YGK','FACULTY')")
    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.getEvaluationById(id));
    }

    @PreAuthorize("hasAnyRole('OIDB','YGK','FACULTY')")
    @GetMapping
    public List<EvaluationResponseDto> getAll() {
        return evaluationService.getAll();
    }
}

