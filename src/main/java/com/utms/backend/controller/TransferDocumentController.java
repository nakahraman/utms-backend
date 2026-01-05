package com.utms.backend.controller;

import com.utms.backend.model.dto.TransferDocumentResponseDto;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.service.TransferDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Transfer document APIs")
public class TransferDocumentController {

    private final TransferDocumentService documentService;

    public TransferDocumentController(TransferDocumentService documentService) {
        this.documentService = documentService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/upload")
    @Operation(summary = "Upload document", description = "Student uploads document for application")
    public TransferDocumentResponseDto uploadDocument(@RequestParam Long appId,
                                                      @RequestParam DocumentType documentType,
                                                      @RequestParam MultipartFile file) throws Exception {
        return documentService.uploadDocument(appId, documentType, file);
    }
}
