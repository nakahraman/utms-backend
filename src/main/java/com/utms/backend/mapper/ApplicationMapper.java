package com.utms.backend.mapper;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponseDto map(Application a) {

        String studentName = a.getStudent() != null ? a.getStudent().getName() : null;
        String departmentName = a.getDepartment() != null ? a.getDepartment().getDeptName() : null;

        String facultyName = null;
        if (a.getDepartment() != null && a.getDepartment().getFaculty() != null) {
            facultyName = a.getDepartment().getFaculty().getFacultyName();
        }

        return new ApplicationResponseDto(
                a.getAppId(),
                studentName,
                departmentName,
                facultyName,
                a.getGpa(),
                a.getStatus(),
                a.getValidationStatus(),
                a.getSubmissionDate(),
                a.getDecision()
        );
    }
}
