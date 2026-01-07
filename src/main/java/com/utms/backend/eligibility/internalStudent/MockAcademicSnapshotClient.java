package com.utms.backend.eligibility.internalStudent;

import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import org.springframework.stereotype.Service;

@Service
public class MockAcademicSnapshotClient implements ExternalAcademicSnapshotClient {

    @Override
    public AcademicEligibilitySnapshot fetchSnapshot(String studentNo, Long departmentId) {

        AcademicEligibilitySnapshot s = new AcademicEligibilitySnapshot();
        s.setCompletedSemesters(4);
        s.setHasLeaveSemester(false);
        s.setHasFailedCourse(false);
        s.setGpa(3.25);
        s.setSuccessRank(18000);
        s.setTargetSemester(5);
        s.setHasPortfolio(true);

        return s;
    }
}
