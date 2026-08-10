package com.sujal.itsm.ticketing.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.ticketing.model.Comment;
import com.sujal.itsm.ticketing.model.Ticket;

/**
 * Repository for Ticket Comments.
 *
 * <p>Enterprise Features: - Specification support - Pagination - Dashboard statistics - Activity
 * history
 */
@Repository
public interface CommentRepository
    extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

  // ==========================================================
  // Ticket Comments
  // ==========================================================

  List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);

  Page<Comment> findByTicket(Ticket ticket, Pageable pageable);

  // ==========================================================
  // Author (String-based, matching the Comment entity's 'authorName' field)
  // ==========================================================

  List<Comment> findByAuthorName(String authorName);

  Page<Comment> findByAuthorName(String authorName, Pageable pageable);

  // ==========================================================
  // Dashboard Metrics
  // ==========================================================

  long countByTicket(Ticket ticket);

  long countByAuthorName(String authorName);
}
