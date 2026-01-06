package com.utms.backend.eligibility.externalStudent;

import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;

public interface ExternalEligibilityExtractor {
    AcademicEligibilitySnapshot extract(Application app);
}
