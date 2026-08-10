package com.sujal.itsm.itams.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.AssetCondition;
import com.sujal.itsm.itams.enums.AssetStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "assets")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Size(max = 50)
  @Column(name = "asset_tag", nullable = false, unique = true, length = 50)
  private String assetTag;

  @Size(max = 100)
  @Column(name = "serial_number", length = 100)
  private String serialNumber;

  @NotBlank(message = "Asset name is required")
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private AssetCategory category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id")
  private Brand brand;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  // Purchase Info
  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Positive(message = "Purchase price must be positive")
  @Column(name = "purchase_price", precision = 10, scale = 2)
  private BigDecimal purchasePrice;

  @Size(max = 100)
  @Column(name = "invoice_number", length = 100)
  private String invoiceNumber;

  @Size(max = 100)
  @Column(name = "po_number", length = 100)
  private String poNumber;

  // Warranty
  @Column(name = "warranty_start_date")
  private LocalDate warrantyStartDate;

  @Column(name = "warranty_end_date")
  private LocalDate warrantyEndDate;

  @Column(name = "amc_end_date")
  private LocalDate amcEndDate;

  // Status & Condition
  @NotNull(message = "Status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Builder.Default
  private AssetStatus status = AssetStatus.AVAILABLE;

  @NotNull(message = "Condition is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "asset_condition", nullable = false, length = 50)
  @Builder.Default
  private AssetCondition condition = AssetCondition.NEW;

  @Size(max = 255)
  @Column(length = 255)
  private String location;

  // QR Code
  @Size(max = 255)
  @Column(name = "qr_code_url", length = 255)
  private String qrCodeUrl;

  // Metadata
  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  // Audit
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private AppUser createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private AppUser updatedBy;

  /**
   * Check if warranty is still valid (not expired)
   */
  public boolean warrantyValid() {
    if (warrantyEndDate == null) {
      return false;
    }
    return warrantyEndDate.isAfter(java.time.LocalDate.now()) ||
            warrantyEndDate.isEqual(java.time.LocalDate.now());
  }

  /**
   * Check if AMC is still valid (not expired)
   */
  public boolean amcValid() {
    if (amcEndDate == null) {
      return false;
    }
    return amcEndDate.isAfter(java.time.LocalDate.now()) ||
            amcEndDate.isEqual(java.time.LocalDate.now());
  }

  /**
   * Check if asset is available for allocation
   */
  public boolean isAvailable() {
    return this.status == com.sujal.itsm.itams.enums.AssetStatus.AVAILABLE;
  }

  /**
   * Check if asset is assigned
   */
  public boolean isAssigned() {
    return this.status == com.sujal.itsm.itams.enums.AssetStatus.ASSIGNED;
  }

  /**
   * Get days remaining until warranty expires
   */
  public Long getWarrantyDaysRemaining() {
    if (warrantyEndDate == null) {
      return null;
    }
    return java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(),
            warrantyEndDate
    );
  }
}
