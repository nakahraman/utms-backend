package com.utms.backend.repository;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferDocumentRepository extends JpaRepository<TransferDocument, Long> {
    List<TransferDocument> findByApplication_AppId(Long appId);

    boolean existsByApplication_AppIdAndDocumentType(Long appId, DocumentType documentType);

}
