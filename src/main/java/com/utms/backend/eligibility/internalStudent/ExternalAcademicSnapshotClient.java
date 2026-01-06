package com.utms.backend.eligibility.internalStudent;

import com.utms.backend.model.entities.AcademicEligibilitySnapshot;

public interface ExternalAcademicSnapshotClient {
    AcademicEligibilitySnapshot fetchSnapshot(String studentNo, Long departmentId);
}
