package com.utms.backend.mapper;

import com.utms.backend.model.dto.TransferDocumentResponseDto;
import com.utms.backend.model.entities.TransferDocument;
import org.springframework.stereotype.Component;

@Component
public class TransferDocumentMapper {

    public TransferDocumentResponseDto map(TransferDocument d) {
        return new TransferDocumentResponseDto(
                d.getDocumentId(),
                d.getApplication() != null ? d.getApplication().getAppId() : null,
                d.getDocumentType(),
                d.getFileName()
        );
    }
}
