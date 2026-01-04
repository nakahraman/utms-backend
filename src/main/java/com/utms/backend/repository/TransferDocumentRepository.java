package com.utms.backend.repository;

import com.utms.backend.model.entities.TransferDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferDocumentRepository extends JpaRepository<TransferDocument, Long> {
}
