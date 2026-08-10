package com.sujal.itsm.ticketing.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise Attachment Entity Represents a file uploaded and attached to a support ticket.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Entity
@Table(
    name = "attachments",
    indexes = {
      @Index(name = "idx_attachment_ticket_id", columnList = "ticket_id"),
      @Index(name = "idx_attachment_uploaded_at", columnList = "uploaded_at")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

  // ============================================
  // IDENTITY
  // ============================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ============================================
  // FILE METADATA
  // ============================================

  /** The original name of the file as provided by the user. */
  @NotBlank(message = "Filename is required")
  @Size(max = 255, message = "Filename must be less than 255 characters")
  @Column(name = "filename", nullable = false, length = 255)
  private String filename;

  /**
   * The unique, system-generated name used for physical storage. Prevents filename collisions and
   * directory traversal attacks.
   */
  @NotBlank(message = "Stored filename is required")
  @Size(max = 255, message = "Stored filename must be less than 255 characters")
  @Column(name = "stored_filename", nullable = false, length = 255)
  private String storedFilename;

  /** The MIME type of the file (e.g., "image/png", "application/pdf"). */
  @NotBlank(message = "Content type is required")
  @Size(max = 100, message = "Content type must be less than 100 characters")
  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  /** The size of the file in bytes. */
  @Min(value = 0, message = "File size cannot be negative")
  @Column(name = "size", nullable = false)
  private long size;

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
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ============================================
  // HELPER METHODS (UI & DISPLAY)
  // ============================================

  /**
   * Checks if the attachment is an image file. Useful for rendering image previews in the UI.
   *
   * @return true if content type starts with "image/"
   */
  @Transient
  public boolean isImage() {
    return contentType != null && contentType.startsWith("image/");
  }

  /**
   * Checks if the attachment is a PDF document.
   *
   * @return true if content type is "application/pdf"
   */
  @Transient
  public boolean isPdf() {
    return "application/pdf".equals(contentType);
  }

  /**
   * Gets the file size in Kilobytes (KB).
   *
   * @return size in KB
   */
  @Transient
  public double getSizeInKB() {
    return (double) size / 1024.0;
  }

  /**
   * Gets the file size in Megabytes (MB).
   *
   * @return size in MB
   */
  @Transient
  public double getSizeInMB() {
    return (double) size / (1024.0 * 1024.0);
  }

  /**
   * Gets a human-readable file size string (e.g., "1.5 MB", "250 KB", "800 B"). Eliminates the need
   * for complex formatting logic in Thymeleaf templates.
   *
   * @return formatted size string
   */
  @Transient
  public String getFormattedSize() {
    if (size < 1024) {
      return size + " B";
    } else if (size < 1024 * 1024) {
      return String.format("%.1f KB", getSizeInKB());
    } else {
      return String.format("%.2f MB", getSizeInMB());
    }
  }

  /**
   * Extracts the file extension from the original filename. Useful for displaying file type icons
   * (e.g., .pdf, .docx, .png).
   *
   * @return the file extension (without the dot), or empty string if none
   */
  @Transient
  public String getFileExtension() {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
