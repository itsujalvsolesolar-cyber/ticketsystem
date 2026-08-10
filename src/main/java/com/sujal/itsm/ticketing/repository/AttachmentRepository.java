package com.sujal.itsm.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.ticketing.model.Attachment;
import com.sujal.itsm.ticketing.model.Ticket;

/**
 * Repository for Attachment entities.
 *
 * <p>Enterprise Features: - Specification support - Optimized entity loading - Pagination - File
 * lookup - Ticket attachment management
 */
@Repository
public interface AttachmentRepository
    extends JpaRepository<Attachment, Long>, JpaSpecificationExecutor<Attachment> {

  // ==========================================================
  // Basic Lookup
  // ==========================================================

  @Override
  @EntityGraph(attributePaths = {"ticket"})
  Optional<Attachment> findById(Long id);

  // ==========================================================
  // Ticket Attachments
  // ==========================================================

  List<Attachment> findByTicketOrderByUploadedAtAsc(Ticket ticket);

  Page<Attachment> findByTicket(Ticket ticket, Pageable pageable);

  long countByTicket(Ticket ticket);

  // ==========================================================
  // File Lookup
  // ==========================================================

  Optional<Attachment> findByStoredFilename(String storedFilename);

  List<Attachment> findByFilenameContainingIgnoreCase(String filename);

  // ==========================================================
  // Content Type
  // ==========================================================

  List<Attachment> findByContentTypeStartingWith(String contentTypePrefix);

  // ==========================================================
  // Size Queries
  // ==========================================================

  List<Attachment> findBySizeGreaterThan(long size);
}
