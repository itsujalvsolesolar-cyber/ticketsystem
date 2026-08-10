package com.sujal.itsm.workflow.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.workflow.enums.ApprovalActionType;
import com.sujal.itsm.workflow.enums.SignatureType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_actions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalAction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @Column(nullable = false)
    private Integer stepOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id", nullable = false)
    private AppUser approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalActionType action; // APPROVED, REJECTED, DELEGATED

    @Size(max = 500)
    @Column(length = 500)
    private String remarks;

    // --- SIGNATURE & AUDIT FRAMEWORK ---
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SignatureType signatureType; // ELECTRONIC, DRAWN, UPLOADED, TYPED

    @Size(max = 4000) // Increased to support base64 drawn signatures
    @Column(length = 4000)
    private String signatureData; // Base64 image, typed name, or file path

    @Size(max = 45)
    @Column(length = 45)
    private String ipAddress;

    @Size(max = 100)
    @Column(length = 100)
    private String browser; // e.g., "Chrome 140"

    @Size(max = 100)
    @Column(length = 100)
    private String operatingSystem; // e.g., "Windows 11"

    @Size(max = 100)
    @Column(length = 100)
    private String deviceType; // e.g., "Desktop", "Mobile"

    @Size(max = 255)
    @Column(length = 255)
    private String userAgent; // Full UA string for forensic verification

    @Size(max = 64)
    @Column(length = 64)
    private String hashValue; // SHA-256 hash of the approval record for tamper detection

    @Column(nullable = false)
    private LocalDateTime signedAt;
}