package com.utms.backend.controller;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.service.TransferDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Document upload and management APIs")
public class TransferDocumentController {

    private final TransferDocumentService documentService;

    public TransferDocumentController(TransferDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload document", description = "Upload transfer document for application")
    public TransferDocument uploadDocument(@RequestParam Long appId,
                                           @RequestParam String documentType,
                                           @RequestParam MultipartFile file) throws Exception {
        return documentService.uploadDocument(appId, documentType, file);
    }
}