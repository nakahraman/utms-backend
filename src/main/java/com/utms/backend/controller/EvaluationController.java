package com.utms.backend.controller;

import com.utms.backend.mapper.EvaluationMapper;
import com.utms.backend.model.dto.EvaluationResponseDto;
import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ygk")
@Tag(name = "YGK", description = "Department evaluation APIs")
@AllArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PreAuthorize("hasRole('YGK')")
    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate applications", description = "YGK evaluates department applications based on quota")
    public List<EvaluationResponseDto> evaluate(@RequestParam int quota) {
        return evaluationService.evaluateDepartmentApplications(quota);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponseDto> get(@PathVariable Long id) {
        EvaluationResponseDto e = evaluationService.getEvaluationById(id);
        return ResponseEntity.ok(e);
    }


    @GetMapping
    public List<EvaluationResponseDto> getAll() {
        return evaluationService.getAll();
    }
}
