package com.sujal.itsm.iam.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.model.SoftwareLicense;
import com.sujal.itsm.iam.enums.AccountStatus;
import com.sujal.itsm.iam.enums.EmailProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_accounts", indexes = {
        @Index(name = "idx_email_address", columnList = "email_address", unique = true),
        @Index(name = "idx_email_user", columnList = "user_id"),
        @Index(name = "idx_email_status", columnList = "status")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================
    // IDENTITY & OWNERSHIP
    // ============================================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "email_address", nullable = false, unique = true, length = 150)
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private EmailProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_PROVISIONING;

    // ============================================
    // LICENSE & STORAGE
    // ============================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private SoftwareLicense license; // Links to M365/Google Workspace license pool

    @Column(name = "mailbox_size_mb")
    private Integer mailboxSizeMb;

    @Column(name = "storage_used_mb")
    private Integer storageUsedMb;

    // ============================================
    // SECURITY & COMPLIANCE (Metadata Only)
    // ============================================
    @Column(name = "is_mfa_enabled")
    @Builder.Default
    private Boolean isMfaEnabled = false;

    @Column(name = "recovery_email", length = 150)
    private String recoveryEmail;

    @Column(name = "recovery_phone", length = 50)
    private String recoveryPhone;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;

    // ============================================
    // AUDIT & LIFECYCLE
    // ============================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "provisioned_by", length = 100)
    private String provisionedBy;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;
}