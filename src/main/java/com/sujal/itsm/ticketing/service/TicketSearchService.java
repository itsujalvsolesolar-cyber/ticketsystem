package com.sujal.itsm.ticketing.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.dto.TicketSearchCriteria;
import com.sujal.itsm.ticketing.enums.TicketStatus;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import com.sujal.itsm.ticketing.specification.TicketSpecification;

@Service
public class TicketSearchService {

  private final TicketRepository ticketRepository;
  private final CurrentUserService currentUserService;

  public TicketSearchService(
      TicketRepository ticketRepository, CurrentUserService currentUserService) {
    this.ticketRepository = ticketRepository;
    this.currentUserService = currentUserService;
  }

  public Page<Ticket> search(TicketSearchCriteria criteria) {
    AppUser currentUser = currentUserService.getCurrentUser();

    // ✅ FIX: Filter tickets based on user role
    Long assignedToId = null;

    // IT EXECUTIVE should only see their own assigned tickets
    if (currentUser != null && currentUser.hasRole("IT EXECUTIVE")) {
      assignedToId = currentUser.getId();
    }
    // AGENT should also only see their own assigned tickets
    else if (currentUser != null && currentUser.hasRole("AGENT")) {
      assignedToId = currentUser.getId();
    }
    // ADMIN, IT MANAGER, etc. can see all tickets (assignedToId remains null)

    // Convert String status to Enum (if provided)
    TicketStatus status = null;
    if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
      try {
        status = TicketStatus.valueOf(criteria.getStatus());
      } catch (IllegalArgumentException e) {
        // Invalid status, ignore filter
      }
    }

    // Build the Specification with the assignedToId filter
    Specification<Ticket> spec = TicketSpecification.withFilters(
            assignedToId,
            status,
            criteria.getCategoryId(),
            criteria.getDepartmentId(),
            criteria.getSearch());

    // Map sort fields
    String dbSortBy = mapSortBy(criteria.getSortBy());
    Sort sort = criteria.getSortDir() != null && criteria.getSortDir().equalsIgnoreCase("asc")
            ? Sort.by(dbSortBy).ascending()
            : Sort.by(dbSortBy).descending();

    int page = criteria.getPage();
    int size = criteria.getSize();

    // Ensure valid values
    if (page < 0) page = 0;
    if (size <= 0) size = 10;

    Pageable pageable = PageRequest.of(page, size, sort);

    return ticketRepository.findAll(spec, pageable);
  }

  public List<Ticket> findAllForExport(TicketSearchCriteria criteria) {
    AppUser currentUser = currentUserService.getCurrentUser();

    // ✅ Apply same role-based filtering
    Long assignedToId = null;

    if (currentUser != null && currentUser.hasRole("IT EXECUTIVE")) {
      assignedToId = currentUser.getId();
    } else if (currentUser != null && currentUser.hasRole("AGENT")) {
      assignedToId = currentUser.getId();
    }

    TicketStatus status = null;
    if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
      try {
        status = TicketStatus.valueOf(criteria.getStatus());
      } catch (IllegalArgumentException e) {
        // Invalid status
      }
    }

    Specification<Ticket> spec = TicketSpecification.withFilters(
            assignedToId,
            status,
            criteria.getCategoryId(),
            criteria.getDepartmentId(),
            criteria.getSearch());

    Pageable unpaged = Pageable.unpaged();
    return ticketRepository.findAll(spec, unpaged).getContent();
  }

  private String mapSortBy(String sortBy) {
    if (sortBy == null) return "createdAt";
    return switch (sortBy) {
      case "id" -> "id";
      case "status" -> "status";
      case "createdAt" -> "createdAt";
      case "priority" -> "priority";
      case "title" -> "title";
      default -> "createdAt";
    };
  }
}
