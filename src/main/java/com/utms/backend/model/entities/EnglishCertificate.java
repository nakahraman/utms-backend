package com.utms.backend.model.entities;

import com.utms.backend.model.enums.EnglishCertType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "english_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certId;

    @Enumerated(EnumType.STRING)
    private EnglishCertType type;   // IELTS, TOEFL, ...

    private Double score;

    private String documentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    private String fileName;

    private String filePath;
}
