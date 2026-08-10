package com.sujal.itsm.ticketing.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.model.Ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardView {
  private AppUser currentUser;
  private Page<Ticket> tickets;
  private DashboardSummary summary;
  private List<Category> categories;
  private List<Department> departments;
  private List<AppUser> users;
  private TicketSearchCriteria criteria;

  /** Converts the DTO into a Map for Thymeleaf's model.addAllAttributes() */
  public Map<String, Object> toModel() {
    Map<String, Object> model = new HashMap<>();

    // Current user
    model.put("currentUser", currentUser);

    // Tickets (as List for Thymeleaf iteration)
    model.put("tickets", tickets.getContent());

    // ✅ Safe pagination calculations (handle null values)
    int currentPage = Math.max(0, criteria.getPage());
    int pageSize = Math.max(10, criteria.getSize());

    // Pagination metadata
    model.put("currentPage", currentPage);
    model.put("totalPages", tickets.getTotalPages());
    model.put("totalItems", tickets.getTotalElements());
    model.put("pageSize", pageSize);

    long totalElements = tickets.getTotalElements();
    long startItem = totalElements > 0 ? (long) currentPage * pageSize + 1 : 0;
    long endItem = Math.min((long) (currentPage + 1) * pageSize, totalElements);

    model.put("startItem", startItem);
    model.put("endItem", endItem);

    // Sort parameters
    model.put("sortBy", criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt");
    model.put("sortDir", criteria.getSortDir() != null ? criteria.getSortDir() : "desc");

    // Active filters for UI highlighting
    model.put("selectedStatus", criteria.getStatus());
    model.put("selectedPriority", criteria.getPriority());
    model.put("selectedCategoryId", criteria.getCategoryId());
    model.put("selectedDepartmentId", criteria.getDepartmentId());
    model.put("selectedSearch", criteria.getSearch());

    // Reference data for dropdowns
    model.put("departments", departments);
    model.put("categories", categories);
    model.put("users", users);

    // ✅ KPI Summary (including archived count)
    if (summary != null) {
      model.put("totalCount", summary.getTotalCount());
      model.put("openCount", summary.getOpenCount());
      model.put("resolvedCount", summary.getResolvedCount());
      model.put("closedCount", summary.getClosedCount());
      model.put("archivedCount", summary.getArchivedCount());
    } else {
      // Fallback to 0 if summary is null
      model.put("totalCount", 0L);
      model.put("openCount", 0L);
      model.put("resolvedCount", 0L);
      model.put("closedCount", 0L);
      model.put("archivedCount", 0L);
    }

    // ✅ FIXED: Return the complete model, not a hardcoded Map.of()
    return model;
  }
}
