package com.sujal.itsm.core.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // ✅ ADD THIS IMPORT
import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.user.model.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "core_notifications", indexes = {
        @Index(name = "idx_user_read", columnList = "user_id, is_read")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // ✅ ADD THIS ANNOTATION TO PREVENT SERIALIZATION ERROR
    private AppUser user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(length = 50)
    private String module;

    @Column(name = "reference_id")
    private Long referenceId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}