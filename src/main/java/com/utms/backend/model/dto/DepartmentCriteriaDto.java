package com.utms.backend.model.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class DepartmentCriteriaDto {

    private double minGpa;
    private int maxSuccessRank;
    private List<Integer> allowedSemesters;
    private boolean requiresAllCoursesPassed;
    private boolean requiresPortfolio;
}
