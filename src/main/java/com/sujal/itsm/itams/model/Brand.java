package com.sujal.itsm.itams.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "brands")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Brand name is required")
  @Size(max = 100, message = "Brand name must be less than 100 characters")
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Size(max = 500, message = "Description must be less than 500 characters")
  @Column(length = 500)
  private String description;

  @Size(max = 255)
  @Column(name = "logo_url", length = 255)
  private String logoUrl;

  @Size(max = 255)
  @Column(length = 255)
  private String website;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
