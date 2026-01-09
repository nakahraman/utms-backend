package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.externalIntegration.DocumentVerificationService;
import com.utms.backend.mapper.TransferDocumentMapper;
import com.utms.backend.model.dto.TransferDocumentResponseDto;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.EnglishCertificate;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.EnglishCertificateRepository;
import com.utms.backend.repository.TransferDocumentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.nio.file.Paths;
import java.util.Objects;

@Service
@AllArgsConstructor
public class DocumentService {

    private final TransferDocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentVerificationService documentVerificationService;
    private final TransferDocumentMapper transferDocumentMapper;
    private final EnglishCertificateRepository englishCertificateRepository;

    private final String uploadDir = "uploads/";

    private static final List<String> ALLOWED_TYPES =
            List.of("application/pdf", "image/jpeg");
    @Transactional
    public TransferDocumentResponseDto uploadDocument(Long appId,
                                                      DocumentType documentType,
                                                      MultipartFile file) {

        if (file == null || file.isEmpty())
            throw new BusinessException("DOC-400", "No file selected.");

        if (file.getSize() > 10 * 1024 * 1024)
            throw new BusinessException("DOC-413", "File size exceeds 10 MB limit.");

        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new BusinessException("DOC-400", "Only PDF or JPEG files are allowed.");

        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Application not found."));

        try {
            Path root = Paths.get(uploadDir);
            Files.createDirectories(root);

            documentRepository.deleteByApplication_AppIdAndDocumentType(appId, documentType);

            String ext = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));

            String hashedName = DigestUtils.sha256Hex(
                    file.getOriginalFilename() + System.currentTimeMillis()
            ) + ext;

            Path targetPath = root.resolve(hashedName);

            if (!scanForVirus(file))
                throw new BusinessException("DOC-406", "Uploaded file contains malicious content.");

            file.transferTo(targetPath.toFile());

            TransferDocument doc = new TransferDocument();
            doc.setApplication(application);
            doc.setDocumentType(documentType);
            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(hashedName);   // ← SADECE LOGICAL PATH

            return transferDocumentMapper.map(documentRepository.save(doc));

        } catch (IOException ex) {

            throw new BusinessException("SYS-500", "System error occurred while uploading file.");
        }
    }


    private boolean scanForVirus(MultipartFile file) {
        return true; // stub
    }

    public void validateMandatoryDocuments(Application app) {

        if (app.getStudent().getStudentType() != StudentType.EXTERNAL) return;

        List<TransferDocument> docs =
                documentRepository.findByApplication_AppId(app.getAppId());

        Set<DocumentType> uploadedTypes = docs.stream()
                .map(TransferDocument::getDocumentType)
                .collect(Collectors.toSet());

        // ✅ External submit için zorunlu minimum set
        List<DocumentType> mandatory = List.of(
                DocumentType.TRANSCRIPT,
                DocumentType.YKS_RESULT
        );

        for (DocumentType type : mandatory) {
            if (!uploadedTypes.contains(type)) {
                throw new BusinessException(
                        "DOC-402",
                        "Dış üniversite öğrencileri için zorunlu belge eksik: " + type
                );
            }
        }

        // ✅ Zorunlu belgelerin doğrulanması (MOCK)
        for (TransferDocument doc : docs) {

            // İstersen sadece zorunluları verify et:
            if (!mandatory.contains(doc.getDocumentType())) continue;

            boolean valid = documentVerificationService.verify(doc.getDocumentType(), doc);

            if (!valid) {
                throw new BusinessException(
                        "DOC-403",
                        "Belge doğrulanamadı: " + doc.getFileName()
                );
            }
        }
    }


    public boolean hasDocument(Long appId, DocumentType type) {
        return documentRepository.existsByApplication_AppIdAndDocumentType(appId, type);
    }

    public void saveMockDocument(Application app, DocumentType type, byte[] content) {

        try {
            Path root = Paths.get(uploadDir);
            Files.createDirectories(root);

            String name = "AUTO_" + type.name() + "_" + app.getAppId() + ".pdf";
            Path path = root.resolve(name);

            Files.write(path, content);

            TransferDocument doc = new TransferDocument();
            doc.setApplication(app);
            doc.setDocumentType(type);
            doc.setFileName(name);
            doc.setFilePath(name);   // ← LOGICAL PATH

            documentRepository.save(doc);

        } catch (IOException ex) {

            throw new BusinessException("SYS-500", "Mock document generation failed.");
        }
    }


    public EnglishCertificate getEnglishCertificate(Long appId) {

        return englishCertificateRepository.findByApplication_AppId(appId)
                .orElseThrow(() ->
                        new BusinessException("DOC-404",
                                "English certificate not found for application"));
    }


    public ResponseEntity<Resource> download(Long docId) {

        TransferDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new BusinessException("DOC-404","Document not found"));

        Path filePath = Paths.get(uploadDir).resolve(doc.getFilePath());

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable())
                throw new BusinessException("DOC-404","File not found on server");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + doc.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (MalformedURLException ex) {

            throw new BusinessException("SYS-500","File download failed.");
        }
    }

    public Optional<TransferDocument> findDocument(Long appId, DocumentType documentType) {

        return documentRepository
                .findByApplication_AppIdAndDocumentType(appId, documentType);
    }


}
