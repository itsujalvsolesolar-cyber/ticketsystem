package com.sujal.itsm.core.offboarding.model;

import com.sujal.itsm.core.offboarding.enums.ClearanceDepartment;
import com.sujal.itsm.core.offboarding.enums.ClearanceStatus;
import com.sujal.itsm.core.user.model.AppUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_clearances")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClearanceChecklist {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offboarding_request_id", nullable = false)
    private OffboardingRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 20)
    private ClearanceDepartment department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClearanceStatus status = ClearanceStatus.PENDING;

    // The user who digitally signed and cleared this department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleared_by")
    private AppUser clearedBy;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // Base64 encoded digital signature image
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;
}