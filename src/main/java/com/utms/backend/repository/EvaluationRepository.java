package com.utms.backend.repository;

import com.utms.backend.model.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByApplication_AppId(Long appId);
}
