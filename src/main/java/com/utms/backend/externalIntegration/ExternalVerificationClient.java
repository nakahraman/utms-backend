package com.utms.backend.externalIntegration;

import org.springframework.stereotype.Service;

@Service
public class ExternalVerificationClient {

    public boolean verifyExamScore(String studentNumber) {
        return true;
    }

    public boolean verifyEnglishProficiency(String studentNumber) {
        return true;
    }

    public double getConvertedGpa(String studentNumber) {
        return 2.75;
    }
}
