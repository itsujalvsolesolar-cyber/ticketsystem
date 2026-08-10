package com.sujal.itsm.core.audit.repository;

import com.sujal.itsm.core.audit.enums.AuditAction;
import com.sujal.itsm.core.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Dashboard KPIs
    long countByAction(AuditAction action);
    long countByModule(String module);

    // Advanced Search for Audit Viewer
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:module IS NULL OR a.module = :module) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:performedBy IS NULL OR a.performedBy LIKE %:performedBy%) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("module") String module,
            @Param("action") AuditAction action,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}