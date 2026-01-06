package com.utms.backend.eligibility.internalStudent;

import com.utms.backend.model.dto.DepartmentCriteriaDto;
import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import org.springframework.stereotype.Service;

@Service
public class AcademicEligibilityEvaluator {

    public boolean isEligible(AcademicEligibilitySnapshot s, DepartmentCriteriaDto c) {

        if (s.getGpa() < c.getMinGpa()) return false;

        if (s.getSuccessRank() > c.getMaxSuccessRank()) return false;

        if (!c.getAllowedSemesters().contains(s.getTargetSemester())) return false;

        if (c.isRequiresAllCoursesPassed() && s.isHasFailedCourse()) return false;

        if (c.isRequiresPortfolio() && !s.isHasPortfolio()) return false;

        return true;
    }
}

