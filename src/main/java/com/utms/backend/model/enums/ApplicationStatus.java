package com.utms.backend.model.enums;
public enum ApplicationStatus {

    DRAFT,
    SUBMITTED,

    // ÖİDB doğrulama
    OIDB_VALIDATED,
    OIDB_REJECTED,
    OIDB_CRITERIA_REJECTED,
    RESULT_PUBLISHED,

    // External student – YDYO
    SENT_TO_YDYO,
    YDYO_APPROVED,
    YDYO_FAILED,
    YDYO_EXAM_REQUIRED,

    FACULTY_RETURNED,
    YGK_APPROVED,
    YGK_REJECTED,

    // Faculty / YGK
    FACULTY_EVALUATED,
    RETURNED_TO_OIDB,
    WAITLISTED,
    SENT_TO_YGK,
}
