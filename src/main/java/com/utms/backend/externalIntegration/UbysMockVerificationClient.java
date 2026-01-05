package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.stereotype.Service;

@Service
public class UbysMockVerificationClient implements ExternalDocumentVerificationClient {

    @Override
    public boolean verify(DocumentType type, TransferDocument doc) {
        if (type != DocumentType.TRANSCRIPT) return false;

        return doc.getFileName().toLowerCase().contains("transcript");
    }
}
