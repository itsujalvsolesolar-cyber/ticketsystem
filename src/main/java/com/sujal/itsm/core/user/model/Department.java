package com.sujal.itsm.core.user.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise Department Entity Represents an organizational department within the company.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Entity
@Table(
    name = "departments",
    indexes = {@Index(name = "idx_department_name", columnList = "name")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

  // ============================================
  // IDENTITY
  // ============================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // CORE ATTRIBUTES
  // ============================================

  @NotBlank(message = "Department name is required")
  @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
  @Column(unique = true, nullable = false, length = 100)
  private String name;

  @Size(max = 500, message = "Description must be less than 500 characters")
  @Column(length = 500)
  private String description;

  // ============================================
  // AUDIT FIELDS
  // ============================================

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
