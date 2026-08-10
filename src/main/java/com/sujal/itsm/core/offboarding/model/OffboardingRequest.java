package com.sujal.itsm.core.offboarding.model;

import com.sujal.itsm.core.offboarding.enums.OffboardingStatus;
import com.sujal.itsm.core.user.model.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_requests")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OffboardingRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The employee leaving the organization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    // The reporting manager who must approve the handover
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private AppUser manager;

    // HR or Admin who initiated the process
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private AppUser initiatedBy;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "last_working_day")
    private LocalDate lastWorkingDay;

    @Column(length = 255)
    private String reason; // Resignation, Termination, Retirement, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OffboardingStatus status = OffboardingStatus.PENDING;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}