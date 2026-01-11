package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProtectedFileStorageService {

    @Value("${app.storage.uploadDir}")
    private String uploadDir;

    private final CryptoService cryptoService;

    public StoredEncryptedFile storeEncrypted(MultipartFile file, Long appId, DocumentType type) {

        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(base);

            String ext = resolveExt(file.getContentType()); // .pdf/.jpg
            String safeName = "APP_" + appId + "_" + type.name() + "_" + UUID.randomUUID() + ext + ".enc";

            Path target = base.resolve(safeName).normalize();
            if (!target.startsWith(base)) {
                throw new BusinessException("FILE-400", "Invalid file path.");
            }

            byte[] plain = file.getBytes();
            CryptoService.EncryptedPayload payload = cryptoService.encrypt(plain);

            Files.write(target, payload.cipherBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return new StoredEncryptedFile(
                    safeName,
                    target.toString(),
                    file.getSize(),
                    file.getContentType(),
                    Base64.getEncoder().encodeToString(payload.iv()),
                    payload.alg()
            );

        } catch (IOException ex) {
            throw new BusinessException("FILE-500", "Document storage failed.");
        }
    }

    public byte[] readAndDecrypt(String storedEncFileName, String ivBase64) {
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = base.resolve(storedEncFileName).normalize();
            if (!target.startsWith(base)) {
                throw new BusinessException("FILE-400", "Invalid file path.");
            }

            byte[] cipherBytes = Files.readAllBytes(target);
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            return cryptoService.decrypt(cipherBytes, iv);

        } catch (IOException ex) {
            throw new BusinessException("FILE-500", "File read failed.");
        }
    }

    private String resolveExt(String contentType) {
        if (contentType == null) return ".bin";
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            default -> ".bin";
        };
    }

    public record StoredEncryptedFile(
            String storedName, String absolutePath, long originalSize, String contentType,
            String ivBase64, String alg
    ) {}
}

