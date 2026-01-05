package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.stereotype.Service;

@Service
public class UbysMockVerificationClient implements ExternalDocumentVerificationClient {

    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.TRANSCRIPT;
    }

    @Override
    public boolean verify(TransferDocument doc) {
        return doc.getFileName().toLowerCase().contains("transcript");
    }
}
