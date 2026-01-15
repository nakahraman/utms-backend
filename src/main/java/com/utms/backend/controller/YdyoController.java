package com.utms.backend.controller;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.dto.ApplicationResponseDto;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.ApplicationStatus;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.service.ApplicationService;
import com.utms.backend.service.DocumentService;
import com.utms.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/ydyo")
@RequiredArgsConstructor
@Tag(name = "YDYO", description = "Language assessment APIs")
public class YdyoController {

    private final ApplicationService applicationService;
    private final DocumentService documentService;

    private final FileStorageService storageService;

    @PreAuthorize("hasRole('YDYO')")
    @GetMapping("/inbox")
    public List<ApplicationResponseDto> getInbox() {
        return applicationService.getApplicationsByStatus(ApplicationStatus.SENT_TO_YDYO);
    }

    @PreAuthorize("hasRole('YDYO')")
    @PostMapping("/validate")
    public ApplicationResponseDto validate(@RequestParam Long appId) {
        return applicationService.validateYdyo(appId);
    }

    @PreAuthorize("hasRole('YDYO')")
    @PostMapping("/placement-exam")
    public ApplicationResponseDto submitPlacementExam(@RequestParam Long appId,
                                                      @RequestParam boolean passed) {
        return applicationService.submitPlacementExamResult(appId, passed);
    }

    @PreAuthorize("hasRole('YDYO')")
    @GetMapping("/ydyo/document")
    public ResponseEntity<byte[]> downloadForYdyo(@RequestParam Long appId,
                                                  @RequestParam DocumentType type) {

        TransferDocument doc = documentService.getTransferDocument(appId, type)
                .orElseThrow(() -> new BusinessException("DOC-405", "Belge bulunamadı"));

        byte[] fileBytes = storageService.readFile(doc.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(fileBytes);
    }


}

