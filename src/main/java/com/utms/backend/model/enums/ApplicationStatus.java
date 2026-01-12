package com.utms.backend.model.enums;
public enum ApplicationStatus {

    DRAFT,
    SUBMITTED,

    // ÖİDB doğrulama
    OIDB_VALIDATED,
    OIDB_FLAGGED,
    OIDB_RETURNED,
    RESULT_PUBLISHED,

    // External student – YDYO
    SENT_TO_YDYO,
    YDYO_APPROVED,
    YDYO_EXAM_REQUIRED,

    FACULTY_RETURNED,
    YGK_APPROVED,
    YGK_REJECTED,
    YGK_WAITLISTED,
    ACADEMICALLY_INELIGIBLE,

    // Faculty / YGK
    FACULTY_EVALUATED,
    RETURNED_TO_OIDB,

    SENT_TO_YGK,
}
