package com.utms.backend.model.dto;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.Decision;
import com.utms.backend.model.enums.ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApplicationResponseDto {

    private Long appId;
    private String studentName;
    private String departmentName;
    private String facultyName;
    private Double gpa;
    private ApplicationStatus status;
    private ValidationStatus validationStatus;
    private LocalDateTime submissionDate;
    private Decision decision;   // ← BU OLMADAN GÖRÜNMEZ


}
