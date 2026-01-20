package com.utms.backend.model.record;

public record ExternalProfileRequest(
        Long departmentId,
        Double gpa,
        Double examScore
) {}
