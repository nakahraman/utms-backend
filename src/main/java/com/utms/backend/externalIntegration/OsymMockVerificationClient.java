package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.stereotype.Service;

@Service
public class OsymMockVerificationClient implements ExternalDocumentVerificationClient {

    @Override
    public boolean verify(DocumentType type, TransferDocument doc) {
        if (type != DocumentType.YKS_RESULT) return false;

        return doc.getFileName().contains("osym")
               || doc.getFileName().toLowerCase().contains("exam");
    }
}
