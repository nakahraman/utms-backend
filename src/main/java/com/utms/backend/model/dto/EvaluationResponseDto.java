package com.utms.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EvaluationResponseDto {

    private Long id;
    private String message;
    private LocalDateTime dateSent;

    private Long facultyId;
    private String facultyName;
}
