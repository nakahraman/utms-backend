package com.utms.backend.service;

import com.utms.backend.eligibility.externalStudent.ExternalEligibilityExtractor;
import com.utms.backend.eligibility.internalStudent.InternalEligibilityExtractor;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.*;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.EnglishProficiencyResult;
import com.utms.backend.model.enums.StudentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YgkAcademicEligibilityService {

    private final ExternalEligibilityExtractor externalEligibilityExtractor;
    private final InternalEligibilityExtractor internalEligibilityExtractor;

    public AcademicEligibilitySnapshot extractSnapshot(Application app) {

        if (app.getStudent().getStudentType() == StudentType.EXTERNAL) {
            return externalEligibilityExtractor.extract(app);
        }
        return internalEligibilityExtractor.extract(app);
    }

    public void validate(Application app, Department dept) {

        Student student = app.getStudent();
        DepartmentCriteria criteria = dept.getCriteria();

        AcademicEligibilitySnapshot snapshot = extractSnapshot(app);

        // 1️⃣ GPA
        if (snapshot.getGpa() < criteria.getMinGpa()) {
            throw new BusinessException("YGK-ELIG-001",
                    "YGK rejected – GPA below department minimum.");
        }

        // 2️⃣ Yerleşme Başarı Sırası
        if (student.getSuccessRank() == null ||
            student.getSuccessRank() > criteria.getMaxSuccessRank()) {

            throw new BusinessException("YGK-ELIG-002",
                    "YGK rejected – placement rank does not meet department criteria.");
        }

        // 3️⃣ İngilizce Yeterlilik
        if (app.getEnglishResult() == EnglishProficiencyResult.FAILED) {

            throw new BusinessException("YGK-ELIG-003",
                    "YGK rejected – English proficiency not sufficient.");
        }


        // 4️⃣ Bölüm özel kuralları
        if (criteria.isRequiresAllCoursesPassed() &&
            snapshot.isHasFailedCourse()) {

            throw new BusinessException("YGK-ELIG-004",
                    "YGK rejected – not all required courses passed.");
        }

        if (criteria.isRequiresPortfolio() &&
            !snapshot.isHasPortfolio()) {

            throw new BusinessException("YGK-ELIG-005",
                    "YGK rejected – portfolio requirement not satisfied.");
        }
    }
}
