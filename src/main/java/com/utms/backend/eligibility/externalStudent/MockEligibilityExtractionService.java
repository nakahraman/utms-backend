package com.utms.backend.eligibility.externalStudent;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import org.springframework.stereotype.Service;

@Service
public class MockEligibilityExtractionService {


    public AcademicEligibilitySnapshot extract(Application app) {

        AcademicEligibilitySnapshot s = new AcademicEligibilitySnapshot();

        // 🎯 Deterministic – testlerde her zaman aynı sonucu verir
        s.setCompletedSemesters(4);
        s.setHasLeaveSemester(false);
        s.setHasFailedCourse(false);
        s.setGpa(3.25);
        s.setSuccessRank(18);

        // Hedeflenen bölümün semester bilgisi Application’dan gelir
        s.setTargetSemester(app.getDepartment()
                .getCriteria()
                .getAllowedSemesters()
                .get(0));

        // Portfolio gerekliliği bölüme göre belirlenir
        s.setHasPortfolio(!app.getDepartment()
                .getCriteria()
                .isRequiresPortfolio());

        return s;
    }
}
