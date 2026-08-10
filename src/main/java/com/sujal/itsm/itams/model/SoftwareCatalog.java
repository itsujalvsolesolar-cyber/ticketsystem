package com.sujal.itsm.itams.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "software_catalog")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftwareCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Software name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Microsoft Office 365", "SAP"

    @Column(name = "vendor", length = 100)
    private String vendor; // e.g., "Microsoft", "Adobe"

    @PositiveOrZero
    @Column(name = "total_seats")
    @Builder.Default
    private Integer totalSeats = 0;

    @PositiveOrZero
    @Column(name = "cost_per_seat", precision = 10, scale = 2)
    private BigDecimal costPerSeat;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}