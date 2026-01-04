package com.utms.backend.externalIntegration;

public interface ExternalVerificationClient {

    boolean verifyExamScore(String studentNumber);

    boolean verifyEnglishProficiency(String studentNumber);

    double getConvertedGpa(String studentNumber);
}
