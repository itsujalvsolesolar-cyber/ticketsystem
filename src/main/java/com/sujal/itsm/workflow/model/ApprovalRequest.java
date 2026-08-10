package com.sujal.itsm.workflow.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.workflow.enums.ApprovalStatus;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "approval_requests", indexes = {
        @Index(name = "idx_approval_ref", columnList = "reference_id, module_type"),
        @Index(name = "idx_approval_status", columnList = "status")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Changed to nullable = true since new requests might not have a reference entity yet
    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 50)
    private WorkflowModuleType moduleType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @Column(nullable = false)
    private Integer currentStep = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL)
    @OrderBy("timestamp ASC")
    private List<ApprovalAction> actions;

    // ✅ NEW FIELDS for Email Provisioning and custom requests
    @Column(name = "request_type", length = 50)
    private String requestType;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}