package com.utms.backend.model.record;

import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.ValidationStatus;

public record ApplicationStatusResponse(
        Long appId,
        ApplicationStatus status,
        ValidationStatus validationStatus
) {}

