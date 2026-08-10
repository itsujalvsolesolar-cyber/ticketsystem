package com.sujal.itsm.ticketing.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.ticketing.enums.TicketPriority;
import com.sujal.itsm.ticketing.enums.TicketStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_priority", columnList = "priority"),
                @Index(name = "idx_ticket_created_at", columnList = "created_at"),
                @Index(name = "idx_ticket_assigned_to", columnList = "assigned_to_id"),
                @Index(name = "idx_ticket_department", columnList = "department_id"),
                @Index(name = "idx_ticket_is_deleted", columnList = "is_deleted") // ✅ Added index for soft delete
        })
@SQLRestriction("is_deleted = false") // ✅ Automatically hides deleted tickets from ALL normal queries
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

  // ============================================
  // IDENTITY
  // ============================================
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // CORE ATTRIBUTES
  // ============================================
  @NotBlank(message = "Requester name is required")
  @Size(max = 100, message = "Requester name must be less than 100 characters")
  @Column(name = "requester_name", nullable = false, length = 100)
  private String requesterName;

  @NotBlank(message = "Title is required")
  @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
  @Column(nullable = false, length = 200)
  private String title;

  @Size(max = 5000, message = "Description must be less than 5000 characters")
  @Column(columnDefinition = "TEXT")
  private String description;

  @Size(max = 50, message = "Intercom number must be less than 50 characters")
  @Column(name = "intercom_number", length = 50)
  private String intercomNumber;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  @Builder.Default
  private TicketStatus status = TicketStatus.OPEN;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  @Builder.Default
  private TicketPriority priority = TicketPriority.MEDIUM;

  // ============================================
  // RELATIONSHIPS
  // ============================================
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "assigned_to_id")
  private AppUser assignedTo;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id")
  private Department department;

  @OneToMany(mappedBy = "ticket", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @Builder.Default
  private List<Attachment> attachments = new ArrayList<>();

  // ============================================
  // LIFECYCLE & TIMESTAMPS
  // ============================================
  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============================================
  // SOFT DELETE FIELDS ✅
  // ============================================
  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  // ============================================
  // HELPER METHODS (SLA & DURATION)
  // ============================================
  @Transient
  public Long getDurationInMinutes() {
    if (startedAt == null) return null;
    LocalDateTime end = completedAt != null ? completedAt : LocalDateTime.now();
    return Duration.between(startedAt, end).toMinutes();
  }

  @Transient
  public String getSlaStatus() {
    if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
      return "MET";
    }
    if (createdAt == null) return "UNKNOWN";

    long hoursElapsed = Duration.between(createdAt, LocalDateTime.now()).toHours();
    long slaHours = getSlaHoursForPriority();

    return hoursElapsed > slaHours ? "BREACHED" : "WITHIN_SLA";
  }

  @Transient
  public long getSlaHoursRemaining() {
    if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
      return 0;
    }
    if (createdAt == null) return 0;

    long hoursElapsed = Duration.between(createdAt, LocalDateTime.now()).toHours();
    long slaHours = getSlaHoursForPriority();

    return slaHours - hoursElapsed;
  }

  private long getSlaHoursForPriority() {
    return priority != null ? priority.getSlaHours() : TicketPriority.MEDIUM.getSlaHours();
  }

  @Transient
  public String getFormattedDuration() {
    if (startedAt == null) {
      return "0h 0m";
    }
    LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
    Duration duration = Duration.between(startedAt, endTime);
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    return String.format("%dh %dm", hours, minutes);
  }

  // ============================================
  // LIFECYCLE CALLBACKS
  // ============================================
  @PrePersist
  protected void onCreate() {
    if (status == null) {
      status = TicketStatus.OPEN;
    }
    if (priority == null) {
      priority = TicketPriority.MEDIUM;
    }
  }
}