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
    @Column(nullable = false)
    private EnglishCertType type;   // IELTS, TOEFL, ...

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private String documentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
}
