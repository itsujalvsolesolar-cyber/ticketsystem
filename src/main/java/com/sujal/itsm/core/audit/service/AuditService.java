package com.sujal.itsm.core.audit.service;

import com.sujal.itsm.core.audit.enums.AuditAction;
import com.sujal.itsm.core.audit.model.AuditLog;
import com.sujal.itsm.core.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Asynchronously saves the audit log so it doesn't block the main business transaction.
     * Uses REQUIRES_NEW to ensure the audit is saved even if the main transaction rolls back
     * (optional, but good for security logs like failed logins).
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("✅ Audit log saved: {} - {} by {}", auditLog.getAction(), auditLog.getModule(), auditLog.getPerformedBy());
        } catch (Exception e) {
            log.error("❌ Failed to save audit log: {}", e.getMessage());
        }
    }

    // Helper to build the log quickly
    public AuditLog.AuditLogBuilder createLogBuilder(AuditAction action, String module, String performedBy, String userRole) {
        return AuditLog.builder()
                .action(action)
                .module(module)
                .performedBy(performedBy)
                .userRole(userRole);
    }
}