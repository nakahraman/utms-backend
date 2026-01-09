package com.utms.backend.model.entities;

import com.utms.backend.model.enums.DocumentType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    private String fileName;

    private String filePath;
}
