package com.sujal.itsm.ticketing.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise Category Entity Represents a ticket category with SLA configurations. Categories help
 * classify and prioritize support tickets.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Entity
@Table(
    name = "categories",
    indexes = {
      @Index(name = "idx_category_name", columnList = "name"),
      @Index(name = "idx_category_active", columnList = "isActive")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

  // ============================================
  // IDENTITY
  // ============================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // CORE ATTRIBUTES
  // ============================================

  @NotBlank(message = "Category name is required")
  @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
  @Column(unique = true, nullable = false, length = 100)
  private String name;

  @Size(max = 500, message = "Description must be less than 500 characters")
  @Column(length = 500)
  private String description;

  /**
   * SLA (Service Level Agreement) in hours. Defines the maximum time allowed to resolve tickets in
   * this category. Example: Hardware = 8 hours, Network = 2 hours, Email = 4 hours
   */
  @NotNull(message = "SLA hours is required")
  @Column(name = "sla_hours", nullable = false)
  @Builder.Default
  private Integer slaHours = 8;

  /** Default priority for tickets created in this category. Values: LOW, MEDIUM, HIGH, CRITICAL */
  @Size(max = 20, message = "Priority must be less than 20 characters")
  @Column(name = "default_priority", length = 20)
  @Builder.Default
  private String defaultPriority = "MEDIUM";

  /**
   * Indicates if this category is currently active. Inactive categories cannot be used for new
   * tickets but remain in historical data.
   */
  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private boolean isActive = true;

  /** Display order for UI dropdowns and lists. */
  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  // ============================================
  // AUDIT FIELDS
  // ============================================

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============================================
  // HELPER METHODS
  // ============================================

  /**
   * Checks if this category has a critical SLA (2 hours or less).
   *
   * @return true if SLA is 2 hours or less
   */
  public boolean hasCriticalSla() {
    return slaHours != null && slaHours <= 2;
  }

  /**
   * Checks if this category has a standard SLA (4-8 hours).
   *
   * @return true if SLA is between 4 and 8 hours
   */
  public boolean hasStandardSla() {
    return slaHours != null && slaHours >= 4 && slaHours <= 8;
  }

  /**
   * Checks if this category has a relaxed SLA (more than 8 hours).
   *
   * @return true if SLA is more than 8 hours
   */
  public boolean hasRelaxedSla() {
    return slaHours != null && slaHours > 8;
  }

  /**
   * Gets the SLA urgency level based on hours.
   *
   * @return "CRITICAL", "HIGH", "MEDIUM", or "LOW"
   */
  public String getSlaUrgencyLevel() {
    if (slaHours == null) return "UNKNOWN";
    if (slaHours <= 2) return "CRITICAL";
    if (slaHours <= 4) return "HIGH";
    if (slaHours <= 8) return "MEDIUM";
    return "LOW";
  }
}
