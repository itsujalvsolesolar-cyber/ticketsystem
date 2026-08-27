package com.sujal.itsm.itams.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AssetCondition;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.sujal.itsm.itams.enums.AcceptanceStatus;
import com.sujal.itsm.workflow.enums.SignatureType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_allocations")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Asset being allocated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // The Employee receiving the asset
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // The IT Staff who performed the allocation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocated_by_id")
    private AppUser allocatedBy;

    @Column(name = "allocation_date", nullable = false)
    @Builder.Default
    private LocalDateTime allocationDate = LocalDateTime.now();

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    // Condition of the asset when given to the employee
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_at_issue")
    @Builder.Default
    private AssetCondition conditionAtIssue = AssetCondition.NEW;

    // Condition of the asset when returned (optional, filled on return)
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_at_return")
    private AssetCondition conditionAtReturn;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // If false, the allocation is closed/returned
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ===== REFINED FLOW: Employee acceptance & chain of custody =====
    @Enumerated(EnumType.STRING)
    @Column(name = "acceptance_status", nullable = false, length = 20)
    @Builder.Default
    private AcceptanceStatus acceptanceStatus = AcceptanceStatus.PENDING;

    @Column(length = 500)
    private String accessories; // e.g. "Charger, Laptop Bag, Mouse"

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_id")
    private AppUser acceptedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_type", length = 20)
    private SignatureType signatureType;

    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData;

    @Column(name = "acceptance_ip", length = 45)
    private String acceptanceIp;

    @Column(name = "acceptance_user_agent", length = 255)
    private String acceptanceUserAgent;
}