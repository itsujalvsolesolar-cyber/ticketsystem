package com.sujal.itsm.ticketing.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sujal.itsm.ticketing.dto.TicketSearchCriteria;
import com.sujal.itsm.ticketing.model.Ticket;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class TicketExportService {

  private final TicketSearchService ticketSearchService;

  public TicketExportService(TicketSearchService ticketSearchService) {
    this.ticketSearchService = ticketSearchService;
  }

  public void exportTickets(TicketSearchCriteria criteria, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/csv");
    response.setHeader(
        "Content-Disposition", "attachment; filename=\"tickets_" + LocalDate.now() + ".csv\"");

    List<Ticket> tickets = ticketSearchService.findAllForExport(criteria);

    try (PrintWriter writer = response.getWriter()) {
      writer.println(
          "ID,Title,Description,Status,Priority,Category,Department,Created By,Assigned To,Created At,SLA Status");

      for (Ticket ticket : tickets) {
        writer.println(
            String.format(
                "%d,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s,%s",
                ticket.getId(),
                escapeCsv(ticket.getTitle()),
                escapeCsv(ticket.getDescription()),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory() != null ? ticket.getCategory().getName() : "",
                ticket.getDepartment() != null ? ticket.getDepartment().getName() : "",
                escapeCsv(ticket.getRequesterName()),
                ticket.getAssignedTo() != null
                    ? ticket.getAssignedTo().getUsername()
                    : "Unassigned",
                ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "",
                ticket.getSlaStatus()));
      }
    }
  }

  private String escapeCsv(String value) {
    if (value == null) return "";
    return value.replace("\"", "\"\"");
  }
}
