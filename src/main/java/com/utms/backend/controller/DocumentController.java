package com.utms.backend.controller;

import com.utms.backend.model.dto.TransferDocumentResponseDto;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Transfer document APIs")
@AllArgsConstructor
public class DocumentController {

    private final DocumentService documentService;



    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upload document", description = "Student uploads document for application")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TransferDocumentResponseDto uploadDocument(
            @RequestParam Long appId,
            @RequestParam DocumentType documentType,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return documentService.uploadDocument(appId, documentType, file);
    }

    @GetMapping("/documents/{docId}/download")
    @PreAuthorize("hasAnyRole('STUDENT','OIDB','FACULTY','YGK')")
    public ResponseEntity<Resource> download(@PathVariable Long docId) {
        return documentService.download(docId);
    }

}
