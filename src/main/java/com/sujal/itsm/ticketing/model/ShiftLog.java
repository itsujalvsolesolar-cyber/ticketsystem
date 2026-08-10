package com.sujal.itsm.ticketing.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sujal.itsm.core.user.model.AppUser;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise Shift Log Entity Tracks the working hours, clock-in, and clock-out times of IT staff.
 * Used for attendance tracking, SLA response time calculations, and payroll.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Entity
@Table(
    name = "shift_logs",
    indexes = {
      @Index(name = "idx_shift_user_id", columnList = "user_id"),
      @Index(name = "idx_shift_clock_in", columnList = "clock_in_time"),
      @Index(
          name = "idx_shift_user_active",
          columnList = "user_id, clock_out_time") // Composite index for fast active shift lookups
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftLog {

  // ============================================
  // IDENTITY
  // ============================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // RELATIONSHIPS
  // ============================================

  @NotNull(message = "User is required for shift log")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  // ============================================
  // CORE ATTRIBUTES
  // ============================================

  /** The time the user started their shift. */
  @NotNull(message = "Clock-in time is required")
  @Column(name = "clock_in_time", nullable = false)
  private LocalDateTime clockInTime;

  /** The time the user ended their shift. If null, the shift is currently active. */
  @Column(name = "clock_out_time")
  private LocalDateTime clockOutTime;

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
  // HELPER METHODS (LIFECYCLE & CALCULATIONS)
  // ============================================

  /**
   * Encapsulates the logic to end a shift. Prevents controllers from directly manipulating
   * timestamps.
   */
  public void closeShift() {
    if (this.clockOutTime == null) {
      this.clockOutTime = LocalDateTime.now();
    }
  }

  /**
   * Checks if the shift is currently active (user has not clocked out).
   *
   * @return true if clockOutTime is null
   */
  @Transient
  public boolean isActive() {
    return clockOutTime == null;
  }

  /**
   * Calculates the total duration of the shift in minutes. If active, calculates duration up to the
   * current time.
   *
   * @return Duration in minutes, or 0 if clockInTime is null
   */
  @Transient
  public long getDurationInMinutes() {
    if (clockInTime == null) return 0;
    LocalDateTime end = clockOutTime != null ? clockOutTime : LocalDateTime.now();
    return Duration.between(clockInTime, end).toMinutes();
  }

  /**
   * Calculates the total duration of the shift in hours (truncated).
   *
   * @return Duration in hours, or 0 if clockInTime is null
   */
  @Transient
  public long getDurationInHours() {
    return getDurationInMinutes() / 60;
  }

  /**
   * Gets a human-readable formatted duration string (e.g., "2h 15m", "45m", "8h"). Eliminates the
   * need for complex formatting logic in Thymeleaf templates.
   *
   * @return formatted duration string
   */
  @Transient
  public String getFormattedDuration() {
    if (clockInTime == null) return "N/A";

    long totalMinutes = getDurationInMinutes();
    long hours = totalMinutes / 60;
    long minutes = totalMinutes % 60;

    if (hours > 0 && minutes > 0) {
      return hours + "h " + minutes + "m";
    } else if (hours > 0) {
      return hours + "h";
    } else {
      return minutes + "m";
    }
  }
}
