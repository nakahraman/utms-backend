package com.utms.backend.controller;

import com.utms.backend.model.entities.Evaluation;
import com.utms.backend.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ygk")
@Tag(name = "YGK Evaluation", description = "Department evaluation APIs")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate applications", description = "Evaluate department applications based on quota")
    public List<Evaluation> evaluate(@RequestParam int quota) {
        return evaluationService.evaluateDepartmentApplications(quota);
    }
}