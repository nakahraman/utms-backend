package com.utms.backend.model.enums;
public enum ApplicationStatus {

    SUBMITTED,

    // ÖİDB doğrulama
    VALIDATED,
    RETURNED,

    // External student – YDYO
    SENT_TO_YDYO,
    YDYO_APPROVED,
    YDYO_FAILED,

    // Faculty / YGK
    SENT_TO_DEPARTMENT,
    DEPT_EVALUATED,
    FACULTY_APPROVED,

    // ÖİDB sonuç
    SENT_TO_REGISTRAR,
    APPROVED,
    WAITLISTED,
    REJECTED
}
