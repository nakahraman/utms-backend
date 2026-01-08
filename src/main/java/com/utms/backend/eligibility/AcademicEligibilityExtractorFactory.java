package com.utms.backend.eligibility;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.eligibility.internalStudent.InternalEligibilityExtractor;
import com.utms.backend.model.enums.StudentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcademicEligibilityExtractorFactory {

    private final InternalEligibilityExtractor internal;
    private final ExternalEligibilityExtractor external;

    public AcademicEligibilityExtractor get(StudentType type) {
        return type == StudentType.EXTERNAL ? external : internal;
    }
}
