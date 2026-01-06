package com.utms.backend.model.entities;

import lombok.Data;

@Data
public class AcademicEligibilitySnapshot {

    private int completedSemesters;
    private boolean hasLeaveSemester;
    private boolean hasFailedCourse;
    private double gpa;
    private int successRank;
    private int targetSemester;
    private boolean hasPortfolio;
}
