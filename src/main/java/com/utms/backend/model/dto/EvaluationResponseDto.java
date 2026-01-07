package com.utms.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EvaluationResponseDto {

    private Long evaluationId;
    private Long applicationId;

    private String message;
    private LocalDateTime dateSent;

    private Long facultyId;
    private String facultyName;
}
