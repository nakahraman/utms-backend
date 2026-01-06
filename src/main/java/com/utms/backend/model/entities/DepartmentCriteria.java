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

    @ElementCollection
    @CollectionTable(name = "department_allowed_semesters",
            joinColumns = @JoinColumn(name = "dept_id"))
    @Column(name = "semester")
    private List<Integer> allowedSemesters;

    private boolean requiresAllCoursesPassed;
    private boolean requiresPortfolio;

    public DepartmentCriteriaDto toDto() {
        DepartmentCriteriaDto dto = new DepartmentCriteriaDto();
        dto.setMinGpa(minGpa);
        dto.setMaxSuccessRank(maxSuccessRank);
        dto.setAllowedSemesters(allowedSemesters);
        dto.setRequiresAllCoursesPassed(requiresAllCoursesPassed);
        dto.setRequiresPortfolio(requiresPortfolio);
        return dto;
    }
}
