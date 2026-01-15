package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    public byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new BusinessException("DOC-500", "Dosya okunamadı");
        }
    }
}
