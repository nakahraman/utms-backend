package com.utms.backend.model.entities;


import com.utms.backend.model.dto.DepartmentCriteriaDto;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Embeddable
@Data
public class DepartmentCriteria {

    private double minGpa;
    private int maxSuccessRank;

    private boolean requiresAllCoursesPassed;
    private boolean requiresPortfolio;

    public DepartmentCriteriaDto toDto() {
        DepartmentCriteriaDto dto = new DepartmentCriteriaDto();
        dto.setMinGpa(minGpa);
        dto.setMaxSuccessRank(maxSuccessRank);
        dto.setRequiresAllCoursesPassed(requiresAllCoursesPassed);
        dto.setRequiresPortfolio(requiresPortfolio);
        return dto;
    }
}
