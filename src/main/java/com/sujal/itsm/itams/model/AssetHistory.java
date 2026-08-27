package com.sujal.itsm.itams.model;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AssetHistoryAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_history", indexes = {
        @Index(name = "idx_asset_history_asset", columnList = "asset_id"),
        @Index(name = "idx_asset_history_action", columnList = "action"),
        @Index(name = "idx_asset_history_created", columnList = "created_at")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetHistoryAction action;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private AppUser performedBy;

    @Column(name = "performed_by_name", length = 100)
    private String performedByName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}