package com.sujal.itsm.itams.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "suppliers")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Supplier name is required")
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String name;

  @Size(max = 500)
  @Column(length = 500)
  private String address;

  @Size(max = 20)
  @Column(length = 20)
  private String phone;

  @Email(message = "Email should be valid")
  @Size(max = 100)
  @Column(length = 100)
  private String email;

  @Size(max = 100)
  @Column(name = "contact_person", length = 100)
  private String contactPerson;

  @Size(max = 50)
  @Column(name = "gst_number", length = 50)
  private String gstNumber;

  @Size(max = 500)
  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // One-to-Many relationship with Assets
  @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<Asset> assets = new ArrayList<>();
}
