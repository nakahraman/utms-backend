package com.utms.backend.externalIntegration;

import com.utms.backend.model.enums.DocumentType;

public interface ExternalDocumentProvider {
    boolean supports(DocumentType type);
    byte[] fetchDocument(Long studentId, DocumentType type);
}
