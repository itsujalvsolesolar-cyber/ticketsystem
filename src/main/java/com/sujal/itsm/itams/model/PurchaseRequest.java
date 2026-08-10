package com.sujal.itsm.itams.model;

import com.sujal.itsm.core.user.model.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_requests", indexes = {
        @Index(name = "idx_pr_requester", columnList = "requester_id"),
        @Index(name = "idx_pr_status", columnList = "status")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title; // e.g., "Dell Latitude 5520 Laptop"

    @NotBlank @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String justification;

    @NotNull @DecimalMin(value = "0.01", message = "Cost must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Size(max = 100)
    @Column(length = 100)
    private String preferredVendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PRStatus status = PRStatus.PENDING_APPROVAL;

    // Links to the Approval Engine
    @Column(name = "approval_request_id")
    private Long approvalRequestId;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PRStatus {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        FULFILLED // IT has purchased and added to stock
    }
}