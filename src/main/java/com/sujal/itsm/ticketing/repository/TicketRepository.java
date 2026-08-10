package com.sujal.itsm.ticketing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.enums.TicketPriority;
import com.sujal.itsm.ticketing.enums.TicketStatus;
import com.sujal.itsm.ticketing.model.Ticket;

@Repository
public interface TicketRepository
    extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

  // =========================================================
  // Basic Lookups
  // =========================================================

  // ✅ Base findById - NO collections (prevents MultipleBagFetchException)
  @Override
  @EntityGraph(attributePaths = {"assignedTo", "department", "category"})
  Optional<Ticket> findById(Long id);

  // ✅ For ticket details page - fetches attachments only
  @EntityGraph(attributePaths = {"assignedTo", "department", "category", "attachments"})
  @Query("SELECT t FROM Ticket t WHERE t.id = :id")
  Optional<Ticket> findByIdWithAttachments(@Param("id") Long id);

  // ✅ For ticket details page - fetches comments only
  @EntityGraph(attributePaths = {"assignedTo", "department", "category", "comments"})
  @Query("SELECT t FROM Ticket t WHERE t.id = :id")
  Optional<Ticket> findByIdWithComments(@Param("id") Long id);

  @EntityGraph(attributePaths = {"assignedTo", "department", "category"})
  Page<Ticket> findAll(Pageable pageable);

  // ✅ Find all non-archived tickets
  @EntityGraph(attributePaths = {"assignedTo", "department", "category"})
  Page<Ticket> findByStatusNot(TicketStatus status, Pageable pageable);

  // =========================================================
  // Dashboard Counts (O(1) SQL Aggregation)
  // =========================================================

  long countByStatus(com.sujal.itsm.ticketing.enums.TicketStatus status);

  long countByPriority(TicketPriority priority);

  long countByAssignedTo(AppUser assignedTo);

  long countByCreatedAtAfter(LocalDateTime createdAfter);


  // =========================================================
  // Assignment
  // =========================================================

  List<Ticket> findByAssignedTo(AppUser assignedTo);

  Page<Ticket> findByAssignedTo(AppUser assignedTo, Pageable pageable);

  Page<Ticket> findByAssignedTo_Id(Long userId, Pageable pageable);

  // =========================================================
  // Status
  // =========================================================

  Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

  List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

  // =========================================================
  // Priority
  // =========================================================

  Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);

  // =========================================================
  // Department
  // =========================================================

  Page<Ticket> findByDepartment_Id(Long departmentId, Pageable pageable);

  // =========================================================
  // Category
  // =========================================================

  Page<Ticket> findByCategory_Id(Long categoryId, Pageable pageable);

  // =========================================================
  // Search
  // =========================================================

  @Query(
      """
            SELECT t
            FROM Ticket t
            WHERE
                LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
  Page<Ticket> search(@Param("keyword") String keyword, Pageable pageable);

  // =========================================================
  // Dashboard Analytics
  // =========================================================

  @Query(
      """
            SELECT COUNT(t)
            FROM Ticket t
            WHERE t.status <> com.sujal.itsm.ticketing.enums.TicketStatus.CLOSED
            """)
  long countOpenTickets();

  @Query(
      """
            SELECT COUNT(t)
            FROM Ticket t
            WHERE t.assignedTo IS NULL
            """)
  long countUnassignedTickets();

  @Query(
      """
            SELECT COUNT(t)
            FROM Ticket t
            WHERE t.createdAt >= :since
            """)
  long countCreatedSince(@Param("since") LocalDateTime since);

  long countByStatusNot(TicketStatus status);

  // =========================================================
  // Soft Delete & Recovery (Bypasses @SQLRestriction)
  // =========================================================

  // ✅ Finds a ticket even if it is deleted (for restoration)
  @Query(value = "SELECT * FROM tickets WHERE id = :id", nativeQuery = true)
  Optional<Ticket> findByIdIncludingDeleted(@Param("id") Long id);

  // ✅ Gets all deleted tickets for the "Recycle Bin" UI
  @Query(value = "SELECT * FROM tickets WHERE is_deleted = true ORDER BY deleted_at DESC", nativeQuery = true)
  List<Ticket> findAllDeleted();

}
