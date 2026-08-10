package com.sujal.itsm.core.audit.model;

import com.sujal.itsm.core.audit.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_module", columnList = "module"),
        @Index(name = "idx_audit_user", columnList = "performed_by"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String module; // e.g., "ASSET", "TICKET", "USER"

    @Column(name = "entity_type", length = 50)
    private String entityType; // e.g., "Asset", "AppUser"

    @Column(name = "record_id")
    private Long recordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    // Store as JSON strings
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "changed_fields", columnDefinition = "TEXT")
    private String changedFields; // e.g., "status, assignedTo"

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "browser_info", length = 100)
    private String browserInfo;

    @Column(name = "os_info", length = 100)
    private String osInfo;

    @Column(name = "device_type", length = 20)
    private String deviceType; // DESKTOP, MOBILE, TABLET

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}