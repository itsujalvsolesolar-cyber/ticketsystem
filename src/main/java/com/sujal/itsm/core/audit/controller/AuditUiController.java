package com.sujal.itsm.core.audit.controller;

import com.sujal.itsm.core.audit.enums.AuditAction;
import com.sujal.itsm.core.audit.model.AuditLog;
import com.sujal.itsm.core.audit.repository.AuditLogRepository;
import com.sujal.itsm.core.audit.service.SoftDeleteService;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditUiController {

    private final AuditLogRepository auditLogRepository;
    private final TicketRepository ticketRepository;
    private final SoftDeleteService softDeleteService;

    // ==========================================
    // AUDIT LOG VIEWER
    // ==========================================
    @GetMapping("/viewer")
    public String showAuditViewer(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "timestamp"));

        LocalDateTime start = (startDate != null && !startDate.isEmpty()) ? LocalDateTime.parse(startDate + "T00:00:00") : null;
        LocalDateTime end = (endDate != null && !endDate.isEmpty()) ? LocalDateTime.parse(endDate + "T23:59:59") : null;

        Page<AuditLog> logs = auditLogRepository.searchAuditLogs(module, action, performedBy, start, end, pageable);

        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("module", module);
        model.addAttribute("action", action);
        model.addAttribute("performedBy", performedBy);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("pageTitle", "Audit Log Viewer");

        return "audit/viewer";
    }

    // ==========================================
    // RECYCLE BIN (Tickets Example)
    // ==========================================
    @GetMapping("/recycle-bin")
    public String showRecycleBin(Model model) {
        model.addAttribute("deletedTickets", ticketRepository.findAllDeleted());
        model.addAttribute("pageTitle", "Recycle Bin");
        return "audit/recycle-bin";
    }

    @PostMapping("/recycle-bin/ticket/{id}/restore")
    public String restoreTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            softDeleteService.restoreTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket restored successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to restore: " + e.getMessage());
        }
        return "redirect:/audit/recycle-bin";
    }
}