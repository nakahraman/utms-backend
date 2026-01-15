package com.utms.backend.model.dto;

import com.utms.backend.model.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusHistoryDto {

    private Long id;
    private ApplicationStatus oldStatus;
    private ApplicationStatus newStatus;
    private String changedByRole;
    private LocalDateTime changedAt;
}
