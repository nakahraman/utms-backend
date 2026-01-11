package com.utms.backend.repository;

import com.utms.backend.model.entities.TransferDocument;
import com.utms.backend.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface TransferDocumentRepository extends JpaRepository<TransferDocument, Long> {

    void deleteByApplication_AppIdAndDocumentType(Long appId, DocumentType type);

    List<TransferDocument> findByApplication_AppId(Long appId);

    Optional<TransferDocument> findByApplication_AppIdAndDocumentType(Long appId, DocumentType type);

}
