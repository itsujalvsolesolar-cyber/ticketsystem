package com.sujal.itsm.itams.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "nas_folders", indexes = {@Index(name = "idx_nas_folder_name", columnList = "folderName", unique = true)})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NasFolder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String folderName; // e.g., "HR_Records"

    @NotBlank @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String networkPath; // e.g., "\\NAS\HR\Records"

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String sensitivityLevel; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED

    @Builder.Default
    @Column(nullable = false)
    private Boolean requiresExecutiveApproval = false;

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}