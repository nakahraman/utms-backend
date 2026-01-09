package com.utms.backend.externalIntegration.mock;


import lombok.Data;

@Data
public class MockYksData {

    private int successRank; // YKS / ÖSYS sıralaması
    private double examScore;  // YKS / ÖSYS  puanı
}
