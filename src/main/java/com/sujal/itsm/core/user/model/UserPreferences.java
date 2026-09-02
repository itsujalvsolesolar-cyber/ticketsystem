package com.sujal.itsm.core.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sujal.itsm.core.enums.ThemePreference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Per-user UI preferences (Phase 16.3).
 * 1:1 with AppUser. Global configs remain in SystemSetting.
 */
@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_prefs_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ThemePreference theme = ThemePreference.SYSTEM;

    @Column(name = "sidebar_collapsed", nullable = false)
    @Builder.Default
    private boolean sidebarCollapsed = false;

    @Column(length = 10)
    @Builder.Default
    private String language = "en";

    @Column(length = 64)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "date_format", length = 32)
    @Builder.Default
    private String dateFormat = "dd MMM yyyy";

    /** JSON blob for widget order/visibility on the dashboard. */
    @Column(name = "dashboard_layout", columnDefinition = "TEXT")
    private String dashboardLayout;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}