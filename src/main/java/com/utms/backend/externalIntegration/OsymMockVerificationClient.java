package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.stereotype.Service;

@Service
public class OsymMockVerificationClient implements ExternalDocumentVerificationClient {

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.YKS_RESULT;
    }

    @Override
    public boolean verify(TransferDocument doc) {
        return doc.getFileName().toLowerCase().contains("osym")
               || doc.getFileName().toLowerCase().contains("exam")
                || doc.getFileName().toLowerCase().contains("yks");
    }
}
