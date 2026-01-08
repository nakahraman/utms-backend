package com.utms.backend.externalIntegration;

import lombok.Data;

@Data
public class UbysTranscriptDto {

    private Long studentId;

    private Double gpa;

    /** Öğrencinin tamamladığı dönem sayısı */
    private Integer completedSemesters;

    /** FF, DZ vb. başarısız dersi var mı */
    private boolean hasFailedCourse;

    /** İzin / dondurulmuş dönem geçirmiş mi */
    private boolean hasLeaveSemester;

    /** Başvurduğu hedef yarıyıl */
    private Integer targetSemester;

    /** Üniversite başarı sırası */
    private Integer successRank;
}
