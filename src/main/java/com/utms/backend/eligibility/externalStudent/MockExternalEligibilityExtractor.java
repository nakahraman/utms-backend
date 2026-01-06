package com.utms.backend.eligibility.externalStudent;

import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;
import com.utms.backend.service.TransferDocumentService;
import org.springframework.stereotype.Service;
import com.utms.backend.model.enums.DocumentType;


@Service
public class MockExternalEligibilityExtractor implements ExternalEligibilityExtractor {

    private final TransferDocumentService documentService;

    public MockExternalEligibilityExtractor(TransferDocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public AcademicEligibilitySnapshot extract(Application app) {

        documentService.validateMandatoryDocuments(app);

        AcademicEligibilitySnapshot s = new AcademicEligibilitySnapshot();
        s.setCompletedSemesters(3);
        s.setHasLeaveSemester(false);
        s.setHasFailedCourse(false);
        s.setGpa(2.95);
        s.setSuccessRank(22000);
        s.setTargetSemester(4);
        s.setHasPortfolio(documentService.hasDocument(app.getAppId(), DocumentType.PORTFOLIO));

        return s;
    }
}
