package com.sujal.itsm.core.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions", indexes = {@Index(name = "idx_perm_code", columnList = "code", unique = true)})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String code; // e.g., "itams:asset.create"

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name; // e.g., "Create Asset"

    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String module; // e.g., "ITAM", "NAS", "SYSTEM"

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;
}