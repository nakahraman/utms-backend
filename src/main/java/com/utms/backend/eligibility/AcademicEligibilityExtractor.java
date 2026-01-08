package com.utms.backend.eligibility;

import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;

public interface AcademicEligibilityExtractor {
    AcademicEligibilitySnapshot extract(Application app);
}
