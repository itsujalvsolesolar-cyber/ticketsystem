package com.sujal.itsm.core.offboarding.model;

import com.sujal.itsm.core.offboarding.enums.AssetReturnStatus;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AssetCondition; // Reusing existing ITAMS enum
import com.sujal.itsm.itams.model.Asset;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_return_records")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetReturnRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offboarding_request_id", nullable = false)
    private OffboardingRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    // The IT staff who physically received the device
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private AppUser receivedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_condition", length = 20)
    private AssetCondition condition; // EXCELLENT, GOOD, FAIR, DAMAGED, LOST

    @Column(name = "missing_accessories", length = 500)
    private String missingAccessories; // e.g., "Charger, Mouse, Bag"

    @Column(name = "damage_details", columnDefinition = "TEXT")
    private String damageDetails;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssetReturnStatus status = AssetReturnStatus.PENDING;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;
}