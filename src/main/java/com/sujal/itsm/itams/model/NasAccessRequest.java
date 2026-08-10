package com.sujal.itsm.itams.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.NasPermissionLevel;
import com.sujal.itsm.itams.enums.NasRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "nas_access_requests", indexes = {
        @Index(name = "idx_nas_req_employee", columnList = "employee_id"),
        @Index(name = "idx_nas_req_status", columnList = "status")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NasAccessRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private AppUser employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id", nullable = false)
    private NasFolder folder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NasPermissionLevel permissionLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NasRequestStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by_id")
    private AppUser requestedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_it_id")
    private AppUser approvedByIt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_md_id")
    private AppUser approvedByMd;

    @Builder.Default
    @Column(nullable = false)
    private Boolean notifyOnRevocation = true;

    @Size(max = 500)
    @Column(length = 500)
    private String remarks;

    // ✅ NEW: Temporary Access Fields
    @Builder.Default
    @Column(nullable = false)
    private Boolean isTemporary = false;

    @Column
    private LocalDateTime temporaryEndDate;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}