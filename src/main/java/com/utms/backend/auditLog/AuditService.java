package com.utms.backend.auditLog;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username,
                    String role,
                    String action,
                    String entity,
                    Long entityId,
                    String ip) {

        AuditLog log = AuditLog.builder()
                .username(username)
                .role(role)
                .action(action)
                .entityName(entity)
                .entityId(entityId)
                .ipAddress(ip)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
