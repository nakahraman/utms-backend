package com.utms.backend.externalIntegration.mock;


import lombok.Data;

@Data
public class MockTranscriptData {

    private double gpa;
    private int completedSemesters;
    private boolean hasFailedCourse;
    private boolean hasLeaveSemester;
    private int targetSemester;
}
