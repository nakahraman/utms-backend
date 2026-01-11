package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.DocumentVerificationService;
import com.utms.backend.externalIntegration.mock.MockDocumentParserService;
import com.utms.backend.externalIntegration.mock.MockEnglishCertData;
import com.utms.backend.mapper.TransferDocumentMapper;
import com.utms.backend.model.dto.TransferDocumentResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.EnglishCertificate;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.EnglishCertificateRepository;
import com.utms.backend.repository.TransferDocumentRepository;
import com.utms.backend.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class DocumentService {

    private final TransferDocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentVerificationService documentVerificationService;
    private final TransferDocumentMapper transferDocumentMapper;
    private final EnglishCertificateRepository englishCertificateRepository;
    private final MockDocumentParserService mockDocumentParserService;
    private final ProtectedFileStorageService storageService;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("application/pdf", "image/jpeg");

    @Transactional
    public TransferDocumentResponseDto uploadDocument(Long appId,
                                                      DocumentType documentType,
                                                      MultipartFile file) {

        validateFile(file);

        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Application not found."));

        documentRepository.deleteByApplication_AppIdAndDocumentType(appId, documentType);
        if (documentType == DocumentType.ENGLISH_CERTIFICATE)
            englishCertificateRepository.deleteByApplication_AppId(appId);

        if (!scanForVirus(file))
            throw new BusinessException("DOC-406", "Malicious file detected.");

        // 🔐 ENCRYPTED STORAGE
        ProtectedFileStorageService.StoredEncryptedFile stored =
                storageService.storeEncrypted(file, appId, documentType);

        TransferDocument doc = new TransferDocument();
        doc.setApplication(application);
        doc.setDocumentType(documentType);
        doc.setFileName("DOCUMENT_" + documentType.name());
        doc.setFilePath(stored.storedName());       // .enc file
        doc.setContentType(stored.contentType());
        doc.setSizeBytes(stored.originalSize());
        doc.setEncryptionIv(stored.ivBase64());
        doc.setEncryptionAlg(stored.alg());

        TransferDocument saved = documentRepository.save(doc);

        if (documentType == DocumentType.ENGLISH_CERTIFICATE) {

            EnglishCertificate cert = new EnglishCertificate();
            cert.setApplication(application);
            cert.setFilePath(stored.storedName());
            cert.setFileName("DOCUMENT_ENGLISH_CERTIFICATE");

            MockEnglishCertData parsed =
                    mockDocumentParserService.parseEnglishCertificate(cert);

            cert.setType(parsed.getType());
            cert.setScore(parsed.getScore());
            cert.setDocumentNo(parsed.getDocumentNo());

            englishCertificateRepository.save(cert);
        }

        return transferDocumentMapper.map(saved);
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BusinessException("DOC-400", "No file selected.");

        if (file.getSize() > 10 * 1024 * 1024)
            throw new BusinessException("DOC-413", "File size exceeds 10MB.");

        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new BusinessException("DOC-415", "Only PDF or JPEG files allowed.");
    }

    private String resolveExtension(MultipartFile file) {
        return switch (file.getContentType()) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            default -> throw new BusinessException("DOC-415", "Invalid type");
        };
    }

    private boolean scanForVirus(MultipartFile file) {
        return file.getSize() < 10 * 1024 * 1024;
    }

    public void validateMandatoryDocuments(Application app) {

        List<DocumentType> mandatory = List.of(
                DocumentType.TRANSCRIPT,
                DocumentType.YKS_RESULT,
                DocumentType.ENGLISH_CERTIFICATE
        );

        List<TransferDocument> docs =
                documentRepository.findByApplication_AppId(app.getAppId());

        Set<DocumentType> uploaded = docs.stream()
                .map(TransferDocument::getDocumentType)
                .collect(Collectors.toSet());

        for (DocumentType type : mandatory) {
            if (!uploaded.contains(type))
                throw new BusinessException("DOC-422",
                        "Missing mandatory document: " + type);
        }

        for (TransferDocument doc : docs) {
            if (mandatory.contains(doc.getDocumentType()) &&
                !documentVerificationService.verify(doc.getDocumentType(), doc)) {

                throw new BusinessException("DOC-403",
                        "Document verification failed: " + doc.getDocumentType());
            }
        }
    }

    public ResponseEntity<byte[]> download(Long docId) {

        TransferDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new BusinessException("DOC-404", "Document not found"));

        Long userId = SecurityUtil.getCurrentUserId();
        if (!doc.getApplication().getStudent().getUser().getUserId().equals(userId))
            throw new BusinessException("SEC-403", "Access denied");

        byte[] plain = storageService.readAndDecrypt(doc.getFilePath(), doc.getEncryptionIv());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(plain);
    }


    public ResponseEntity<byte[]> downloadByAppAndType(Long appId, DocumentType type) {

        TransferDocument doc = getTransferDocument(appId, type)
                .orElseThrow(() -> new BusinessException("DOC-404","Document not found"));

        Long userId = SecurityUtil.getCurrentUserId();
        if (!doc.getApplication().getStudent().getUser().getUserId().equals(userId))
            throw new BusinessException("SEC-403","Access denied");

        byte[] plain = storageService.readAndDecrypt(doc.getFilePath(), doc.getEncryptionIv());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(plain);
    }

    public Optional<TransferDocument> getTransferDocument(Long appId, DocumentType documentType) {
        return documentRepository.findByApplication_AppIdAndDocumentType(appId, documentType);
    }

}
