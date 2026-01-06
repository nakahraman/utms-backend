package com.utms.backend.model.enums;
public enum ApplicationStatus {

    DRAFT,
    SUBMITTED,

    // ÖİDB doğrulama
    OIDB_VALIDATED,
    RETURNED,
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

    FACULTY_APPROVED,
    FACULTY_EVALUATED,
    SENT_TO_OIDB,
    RETURNED_TO_OIDB,
    APPROVED,
    WAITLISTED,
    REJECTED,
    SENT_TO_YGK,
    FINALIZED
}
