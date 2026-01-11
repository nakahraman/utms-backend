package com.utms.backend.model.dto;

import com.utms.backend.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransferDocumentResponseDto {

    private Long appId;
    private DocumentType documentType;
    private String fileName;
    private long sizeBytes;
    private LocalDateTime uploadedAt;
}


