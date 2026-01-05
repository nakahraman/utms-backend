package com.utms.backend.controller;

import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ygk")
@Tag(name = "YGK", description = "Department evaluation APIs")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PreAuthorize("hasRole('YGK')")
    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate applications", description = "YGK evaluates department applications based on quota")
    public List<Evaluation> evaluate(@RequestParam int quota) {
        return evaluationService.evaluateDepartmentApplications(quota);
    }
}
