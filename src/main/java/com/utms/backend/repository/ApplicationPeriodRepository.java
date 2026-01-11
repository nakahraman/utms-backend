package com.utms.backend.repository;

import com.utms.backend.model.entities.ApplicationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationPeriodRepository extends JpaRepository<ApplicationPeriod, Long> {
    Optional<ApplicationPeriod> findFirstByActiveTrueOrderByEndsAtDesc();
}
