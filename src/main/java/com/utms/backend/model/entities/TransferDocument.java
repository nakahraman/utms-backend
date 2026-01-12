package com.utms.backend.model.entities;

import com.utms.backend.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_documents")
@Data
public class TransferDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    // KVKK uyumu: orijinal dosya adı saklanmaz
    private String fileName;

    // Sunucudaki random isim
    private String filePath;

    private String contentType;
    private long sizeBytes;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    private String encryptionIv;   // Base64
    private String encryptionAlg;  // "AES/GCM/NoPadding"

}
