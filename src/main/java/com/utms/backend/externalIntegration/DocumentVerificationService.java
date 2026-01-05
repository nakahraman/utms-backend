package com.utms.backend.externalIntegration;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVerificationService {

    private final List<ExternalDocumentVerificationClient> clients;

    public boolean verify(DocumentType type, TransferDocument doc) {

        return clients.stream()
                .filter(c -> c.supports(type))
                .anyMatch(c -> c.verify(doc));
    }

}
