package com.utms.backend.service;

import com.utms.backend.model.entities.EnglishCertificate;
import com.utms.backend.model.enums.EnglishCertType;
import com.utms.backend.repository.EnglishCertificateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class EnglishCertificateService {
    private final EnglishCertificateRepository englishCertificateRepository;

    public boolean isValid(EnglishCertificate cert) {

        if (cert.getType() == EnglishCertType.IELTS)
            return cert.getScore() >= 6.5;

        if (cert.getType() == EnglishCertType.TOEFL)
            return cert.getScore() >= 80;

        if (cert.getType() == EnglishCertType.YDS)
            return cert.getScore() >= 70;

        return false;
    }

    public Optional<EnglishCertificate> getEnglishCertificate(Long appId) {

        return englishCertificateRepository.findByApplication_AppId(appId);
    }
}
