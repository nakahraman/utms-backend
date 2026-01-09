package com.utms.backend.externalIntegration;

import org.springframework.stereotype.Component;

@Component
public class UbysClient {

    public UbysTranscriptDto getTranscript(Long studentId) {

        UbysTranscriptDto dto = new UbysTranscriptDto();
        dto.setStudentId(studentId);
        dto.setGpa(3.12);
        dto.setCompletedSemesters(4);
        dto.setHasFailedCourse(false);
        dto.setHasLeaveSemester(false);
        dto.setTargetSemester(5);
        dto.setSuccessRank(18000);

        return dto;
    }
}
