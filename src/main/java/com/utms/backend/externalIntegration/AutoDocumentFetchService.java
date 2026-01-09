package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.Application;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoDocumentFetchService {

    private final List<ExternalDocumentProvider> providers;
    private final DocumentService documentService;

    public void fetchMissingDocuments(Application app) {

        for (DocumentType type : DocumentType.values()) {

            if (documentService.hasDocument(app.getAppId(), type))
                continue;

            providers.stream()
                    .filter(p -> p.supports(type))
                    .findFirst()
                    .ifPresent(p -> {
                        byte[] data = p.fetchDocument(app.getStudent().getStudentId(), type);
                        documentService.saveMockDocument(app, type, data);
                    });
        }
    }
}
