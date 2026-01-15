package com.utms.backend.mapper;

import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponseDto map(Application a) {

        String departmentName = a.getDepartment() != null ? a.getDepartment().getDeptName() : null;
        String facultyName = null;
        Boolean requiresPortfolio = false;

        if (a.getDepartment() != null) {
            requiresPortfolio = Boolean.TRUE.equals(a.getDepartment().getCriteria().isRequiresPortfolio());

            if (a.getDepartment().getFaculty() != null) {
                facultyName = a.getDepartment().getFaculty().getFacultyName();
            }
        }

        return new ApplicationResponseDto(
                a.getAppId(),
                a.getDepartment().getDeptId(),
                departmentName,
                facultyName,
                a.getGpa(),
                a.getStatus(),
                a.getValidationStatus(),
                a.getSubmissionDate(),
                a.getDecision(),
                requiresPortfolio,
                a.getStudent().getStudentType(),
                a.getStudent().getUser().getName(),
                a.getStudent().getStudentId()
        );
    }
}
