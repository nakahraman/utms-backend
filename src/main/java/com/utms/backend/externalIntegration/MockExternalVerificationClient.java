package com.utms.backend.externalIntegration;

import org.springframework.stereotype.Service;

@Service
public class MockExternalVerificationClient
        implements ExternalVerificationClient {

    @Override
    public boolean verifyExamScore(String studentNumber) {
        return true;   // şimdilik her zaman geçerli
    }

    @Override
    public boolean verifyEnglishProficiency(String studentNumber) {
        return true;
    }

    @Override
    public double getConvertedGpa(String studentNumber) {
        return 2.75;
    }
}
