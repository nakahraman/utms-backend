package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import com.utms.backend.model.enums.StudentType;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.TransferDocumentRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransferDocumentService {

    private final TransferDocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;

    private final String uploadDir = "uploads/";

    public TransferDocumentService(TransferDocumentRepository documentRepository,
                                   ApplicationRepository applicationRepository) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
    }

    private static final List<String> ALLOWED_TYPES =
            List.of("application/pdf", "image/jpeg");

    public TransferDocument uploadDocument(Long appId,
                                           DocumentType documentType,
                                           MultipartFile file) throws Exception {

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("DOC-413", "Dosya boyutu, izin verilen azami 10 MB sınırını aşmaktadır.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("DOC-400", "Sadece PDF veya JPEG dosyaları yüklenebilir.");
        }

        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404","Başvuru bulunamadı"));

        if (app.getStudent().getStudentType() == StudentType.EXTERNAL
            && file == null) {

            throw new BusinessException("DOC-401",
                    "Dış üniversite öğrencileri belgeleri manuel yüklemek zorundadır.");
        }

        Files.createDirectories(Path.of(uploadDir));

        String hashedName = DigestUtils.sha256Hex(
                file.getOriginalFilename() + System.currentTimeMillis()
        );

        File targetFile = new File(uploadDir + hashedName);

        if (!scanForVirus(file)) {
            throw new RuntimeException("Yüklenen dosyada virüs tespit edildi.");
        }

        file.transferTo(targetFile);

        TransferDocument doc = new TransferDocument();
        doc.setApplication(application);
        doc.setDocumentType(documentType);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(targetFile.getAbsolutePath());

        return documentRepository.save(doc);
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

        for (DocumentType type : DocumentType.values()) {
            if (!uploadedTypes.contains(type)) {
                throw new BusinessException(
                        "DOC-402",
                        "Dış üniversite öğrencileri için zorunlu belge eksik: " + type
                );
            }
        }

        for (DocumentType type : DocumentType.values()) {
            if (!uploadedTypes.contains(type.name())) {
                throw new BusinessException(
                        "DOC-402",
                        "Dış üniversite öğrencileri için zorunlu belge eksik: " + type
                );
            }
        }
    }

    public boolean hasDocument(Long appId, DocumentType type) {
        return documentRepository.existsByApplication_AppIdAndDocumentType(appId, type);
    }

}
