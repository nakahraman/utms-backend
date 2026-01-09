package com.utms.backend.externalIntegration.mock;


import com.utms.backend.model.enums.EnglishCertType;
import lombok.Data;

@Data
public class MockEnglishCertData {

    private boolean hasCertificate;
    private EnglishCertType type;   // IELTS / TOEFL /YDS
    private double score;
    private String documentNo;

}
