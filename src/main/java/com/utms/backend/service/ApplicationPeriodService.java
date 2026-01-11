package com.utms.backend.service;

import com.utms.backend.exception.BusinessException;
import com.utms.backend.model.entities.ApplicationPeriod;
import com.utms.backend.repository.ApplicationPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationPeriodService {

    private final ApplicationPeriodRepository repo;

    public ApplicationPeriod getActiveOrThrow() {
        ApplicationPeriod p = repo.findFirstByActiveTrueOrderByEndsAtDesc()
                .orElseThrow(() -> new BusinessException("PER-404",
                        "Transfer application period is not configured."));
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(p.getStartsAt()) || now.isAfter(p.getEndsAt())) {
            throw new BusinessException("PER-403",
                    "Transfer application period is currently closed.");
        }
        return p;
    }
}
