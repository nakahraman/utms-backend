package com.utms.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transfer_documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private Application application;

    private String documentType;   // Transcript, YKS, EnglishCert …

    private String fileName;

    private String filePath;
}
