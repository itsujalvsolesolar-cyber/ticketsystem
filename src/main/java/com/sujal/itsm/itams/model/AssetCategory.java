package com.sujal.itsm.itams.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "asset_categories")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Category name is required")
  @Size(max = 100, message = "Category name must be less than 100 characters")
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Size(max = 500, message = "Description must be less than 500 characters")
  @Column(length = 500)
  private String description;

  @NotBlank(message = "Prefix is required")
  @Pattern(
      regexp = "^[A-Z]{2,5}$",
      message = "Prefix must be 2-5 uppercase letters (e.g., LT, DS, PR)")
  @Column(nullable = false, unique = true, length = 10)
  private String prefix;

  @Column(name = "default_warranty_months")
  @Builder.Default
  private Integer defaultWarrantyMonths = 12;

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
