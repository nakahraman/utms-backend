package com.utms.backend.service;

import com.utms.backend.model.entities.EnglishCertificate;
import com.utms.backend.model.enums.EnglishCertType;
import org.springframework.stereotype.Service;

@Service
public class EnglishScoreService {

    public boolean isValid(EnglishCertificate cert) {

        if (cert.getType() == EnglishCertType.IELTS)
            return cert.getScore() >= 6.5;

        if (cert.getType() == EnglishCertType.TOEFL)
            return cert.getScore() >= 80;

        return false;
    }
}
