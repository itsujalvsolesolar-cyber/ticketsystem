package com.sujal.itsm.ticketing.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.ticketing.dto.DashboardSummary;
import com.sujal.itsm.ticketing.dto.DashboardView;
import com.sujal.itsm.ticketing.dto.TicketSearchCriteria;
import com.sujal.itsm.ticketing.enums.TicketStatus;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.CategoryRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;

@Service
public class DashboardService {

  private final TicketSearchService ticketSearchService;
  private final TicketRepository ticketRepository;
  private final DepartmentRepository departmentRepository;
  private final CategoryRepository categoryRepository;
  private final AppUserRepository appUserRepository;
  private final CurrentUserService currentUserService;

  public DashboardService(
      TicketSearchService ticketSearchService,
      TicketRepository ticketRepository,
      DepartmentRepository departmentRepository,
      CategoryRepository categoryRepository,
      AppUserRepository appUserRepository,
      CurrentUserService currentUserService) {
    this.ticketSearchService = ticketSearchService;
    this.ticketRepository = ticketRepository;
    this.departmentRepository = departmentRepository;
    this.categoryRepository = categoryRepository;
    this.appUserRepository = appUserRepository;
    this.currentUserService = currentUserService;
  }

  public DashboardView loadDashboard(TicketSearchCriteria criteria) {
    AppUser currentUser = currentUserService.getCurrentUser();

    // ✅ Show only ACTIVE tickets by default (OPEN + IN_PROGRESS)
    // Users can still filter to see RESOLVED/CLOSED/ARCHIVED
    if (criteria.getStatus() == null || criteria.getStatus().isBlank()) {
      // Don't set statuses list - let TicketSpecification handle default behavior
      // The specification will exclude RESOLVED, CLOSED, and ARCHIVED by default
    }

    // 1. Get paginated tickets
    Page<Ticket> tickets = ticketSearchService.search(criteria);

    // 2. Get KPI counts
    long totalCount = ticketRepository.countByStatusNot(TicketStatus.ARCHIVED);

    DashboardSummary summary =
        DashboardSummary.builder()
            .totalCount(totalCount)
            .openCount(ticketRepository.countByStatus(TicketStatus.OPEN))
            .resolvedCount(ticketRepository.countByStatus(TicketStatus.RESOLVED))
            .closedCount(ticketRepository.countByStatus(TicketStatus.CLOSED))
            .archivedCount(ticketRepository.countByStatus(TicketStatus.ARCHIVED))
            .build();

    // 3. Get reference data for filters
    List<Department> departments = departmentRepository.findAll();
    List<Category> categories = categoryRepository.findAll();
    List<AppUser> users = appUserRepository.findAll();

    // 4. Assemble and return the view DTO
    return DashboardView.builder()
        .currentUser(currentUser)
        .tickets(tickets)
        .summary(summary)
        .departments(departments)
        .categories(categories)
        .users(users)
        .criteria(criteria)
        .build();
  }
}
