package com.utms.backend.eligibility.internalStudent;

import com.utms.backend.eligibility.AcademicEligibilityExtractor;
import com.utms.backend.externalIntegration.UbysClient;
import com.utms.backend.externalIntegration.UbysMockVerificationClient;
import com.utms.backend.externalIntegration.UbysTranscriptDto;
import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalEligibilityExtractor
        implements AcademicEligibilityExtractor {

    private final UbysClient ubysClient;

    @Override
    public AcademicEligibilitySnapshot extract(Application app) {

        UbysTranscriptDto data =
                ubysClient.getTranscript(app.getStudent().getStudentId());

        AcademicEligibilitySnapshot s = new AcademicEligibilitySnapshot();

        s.setGpa(data.getGpa());
        s.setCompletedSemesters(data.getCompletedSemesters());
        s.setHasFailedCourse(data.isHasFailedCourse());
        s.setHasLeaveSemester(data.isHasLeaveSemester());
        s.setTargetSemester(data.getTargetSemester());
        s.setSuccessRank(data.getSuccessRank());
        s.setHasPortfolio(false);

        return s;
    }
}
