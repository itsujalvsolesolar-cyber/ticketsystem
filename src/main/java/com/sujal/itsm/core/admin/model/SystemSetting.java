package com.sujal.itsm.core.admin.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category", "setting_key"})
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // e.g., "COMPANY", "SECURITY", "EMAIL", "BRANDING"

    @Column(name = "setting_key", nullable = false, length = 100)
    private String key; // e.g., "company_name", "smtp_host", "password_min_length"

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value; // Stored as String, parsed as needed (JSON, Boolean, etc.)

    @Column(name = "data_type", length = 20)
    @Builder.Default
    private String dataType = "STRING"; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}