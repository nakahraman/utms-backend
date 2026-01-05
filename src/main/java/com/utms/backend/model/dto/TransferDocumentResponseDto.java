package com.utms.backend.model.dto;

import com.utms.backend.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferDocumentResponseDto {

    private Long id;
    private Long appId;
    private DocumentType documentType;
    private String fileName;
}

