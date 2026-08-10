package com.sujal.itsm.ticketing.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sujal.itsm.ticketing.enums.TicketStatus;
import com.sujal.itsm.ticketing.model.Ticket;

import jakarta.persistence.criteria.Predicate;

/**
 * Enterprise-grade dynamic query builder for Ticket searches.
 *
 * <p>Benefits over native SQL: - Type-safe (compiler catches errors) - Database-independent (works
 * with MySQL, PostgreSQL, etc.) - Easy to extend (add new filters without touching SQL) - Better
 * maintainability
 */
public class TicketSpecification {

  /**
   * Builds a composite specification from multiple filter criteria. All filters are optional - null
   * values are ignored.
   */
  public static Specification<Ticket> withFilters(
      Long assignedToId, TicketStatus status, Long categoryId, Long departmentId, String search) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 1. Role-based filtering
      if (assignedToId != null) {
        predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
      }

      // 2. Status filtering
      if (status != null) {
        // If a specific status is selected, filter by it
        predicates.add(cb.equal(root.get("status"), status));
      } else {
        // ✅ DEFAULT: Show only ACTIVE tickets (exclude RESOLVED, CLOSED, ARCHIVED)
        predicates.add(
            cb.in(root.get("status")).value(TicketStatus.OPEN).value(TicketStatus.IN_PROGRESS));
      }

      // 3. Category filtering
      if (categoryId != null) {
        predicates.add(cb.equal(root.get("category").get("id"), categoryId));
      }

      // 4. Department filtering
      if (departmentId != null) {
        predicates.add(cb.equal(root.get("department").get("id"), departmentId));
      }

      // 5. Search filtering
      if (search != null && !search.isBlank()) {
        String searchPattern = "%" + search.toLowerCase() + "%";
        Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
        Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
        Predicate requesterMatch = cb.like(cb.lower(root.get("requesterName")), searchPattern);
        predicates.add(cb.or(titleMatch, descMatch, requesterMatch));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private static Specification<Ticket> hasAssignedTo(Long assignedToId) {
    return (root, query, cb) -> {
      if (assignedToId == null) {
        return cb.conjunction(); // No filter
      }
      return cb.equal(root.get("assignedTo").get("id"), assignedToId);
    };
  }

  private static Specification<Ticket> hasStatus(TicketStatus status) {
    return (root, query, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("status"), status);
    };
  }

  private static Specification<Ticket> hasCategory(Long categoryId) {
    return (root, query, cb) -> {
      if (categoryId == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("category").get("id"), categoryId);
    };
  }

  private static Specification<Ticket> hasDepartment(Long departmentId) {
    return (root, query, cb) -> {
      if (departmentId == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("department").get("id"), departmentId);
    };
  }

  private static Specification<Ticket> hasSearch(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + search.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), pattern),
          cb.like(cb.lower(root.get("description")), pattern));
    };
  }
}
