package com.sujal.itsm.ticketing.model;

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
 * Enterprise Comment Entity Represents a comment, update, or internal note added to a support
 * ticket.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Entity
@Table(
    name = "comments",
    indexes = {
      @Index(name = "idx_comment_ticket_id", columnList = "ticket_id"),
      @Index(name = "idx_comment_created_at", columnList = "created_at")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

  // ============================================
  // IDENTITY
  // ============================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // CORE ATTRIBUTES
  // ============================================

  /** The content of the comment. Increased to 5000 chars to allow pasting logs or stack traces. */
  @NotBlank(message = "Comment message is required")
  @Size(min = 1, max = 5000, message = "Comment must be between 1 and 5000 characters")
  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @NotBlank(message = "Author name is required")
  @Size(max = 100, message = "Author name must be less than 100 characters")
  @Column(name = "author_name", nullable = false, length = 100)
  private String authorName;

  /**
   * Enterprise Feature: Distinguishes between public replies (visible to the requester) and
   * internal notes (visible only to IT staff/agents).
   */
  @Column(name = "is_internal", nullable = false)
  @Builder.Default
  private boolean isInternal = false;

  // ============================================
  // RELATIONSHIPS
  // ============================================

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id", nullable = false)
  private Ticket ticket;

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
