package com.utms.backend.externalIntegration.mock;


import lombok.Data;

@Data
public class MockEnglishCertData {

    private boolean hasCertificate;
    private String type;   // IELTS / TOEFL
    private double score;

}
