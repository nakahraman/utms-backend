package com.utms.backend.repository;

import com.utms.backend.model.entities.EnglishCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnglishCertificateRepository extends JpaRepository<EnglishCertificate, Long> {

    Optional<EnglishCertificate> findByApplication_AppId(Long appId);
}
