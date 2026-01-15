package com.utms.backend.model.dto;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.model.enums.ValidationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class ApplicationResponseDto {

    private Long appId;
    private String departmentName;
    private String facultyName;
    private Double gpa;
    private ApplicationStatus status;
    private ValidationStatus validationStatus;
    private LocalDateTime submissionDate;
    private Decision decision;   // ← BU OLMADAN GÖRÜNMEZ
    private Boolean requiresPortfolio;
    private StudentType studentType;

}
