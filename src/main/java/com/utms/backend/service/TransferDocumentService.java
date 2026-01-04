package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.Application;
import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.repository.ApplicationRepository;
import com.utms.backend.repository.TransferDocumentRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
                                           String documentType,
                                           MultipartFile file) throws Exception {

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("DOC-413", "Dosya boyutu, izin verilen azami 10 MB sınırını aşmaktadır.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("DOC-400", "Sadece PDF veya JPEG dosyaları yüklenebilir.");
        }

        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP-404", "Başvuru bulunamadı."));

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

}
