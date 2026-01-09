package com.utms.backend.eligibility.externalStudent;

import com.utms.backend.eligibility.AcademicEligibilityExtractor;
import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.mock.MockDocumentParserService;
import com.utms.backend.model.entities.AcademicEligibilitySnapshot;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalEligibilityExtractor
        implements AcademicEligibilityExtractor {
    private final DocumentService documentService;
    private final MockDocumentParserService parser;

    @Override
    public AcademicEligibilitySnapshot extract(Application app) {

        TransferDocument transcript =
                documentService.findDocument(app.getAppId(), DocumentType.TRANSCRIPT)
                        .orElseThrow(() -> new BusinessException(
                                "DOC-404", "Transcript belgesi bulunamadı"));

        TransferDocument yks =
                documentService.findDocument(app.getAppId(), DocumentType.YKS_RESULT)
                        .orElseThrow(() -> new BusinessException(
                                "DOC-405", "YKS sonuç belgesi bulunamadı"));

        Optional<TransferDocument> portfolioOpt =
                documentService.findDocument(app.getAppId(), DocumentType.PORTFOLIO);

        var tData = parser.parseTranscript(transcript);
        var yData = parser.parseYks(yks);

        boolean hasPortfolio = portfolioOpt
                .map(parser::parsePortfolio)
                .orElse(false);

        AcademicEligibilitySnapshot snapshot = new AcademicEligibilitySnapshot();

        snapshot.setGpa(tData.getGpa());
        snapshot.setCompletedSemesters(tData.getCompletedSemesters());
        snapshot.setHasFailedCourse(tData.isHasFailedCourse());
        snapshot.setHasLeaveSemester(tData.isHasLeaveSemester());
        snapshot.setTargetSemester(tData.getTargetSemester());
        snapshot.setExamScore(yData.getExamScore());
        snapshot.setSuccessRank(yData.getSuccessRank());
        snapshot.setHasPortfolio(hasPortfolio);

        return snapshot;
    }
}

