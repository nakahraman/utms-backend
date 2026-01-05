package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;

public interface ExternalDocumentVerificationClient {

    boolean verify(DocumentType type, TransferDocument doc);
}

