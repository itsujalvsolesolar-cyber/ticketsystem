package com.sujal.itsm.ticketing.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.sujal.itsm.ticketing.model.ShiftLog;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.sujal.itsm.ticketing.model.TicketActivityLog;
import com.sujal.itsm.ticketing.repository.TicketActivityLogRepository;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.ticketing.repository.ShiftLogRepository;
import com.sujal.itsm.ticketing.dto.DashboardView;
import com.sujal.itsm.ticketing.dto.TicketCreateRequest;
import com.sujal.itsm.ticketing.dto.TicketSearchCriteria;
import com.sujal.itsm.ticketing.model.Comment;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.service.*;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class TicketController {

  private final DashboardService dashboardService;
  private final TicketService ticketService;
  private final AttachmentService attachmentService;
  private final TicketCommentService ticketCommentService;
  private final TicketAssignmentService ticketAssignmentService;
  private final TicketExportService ticketExportService;
  private final CurrentUserService currentUserService;
  private final AppUserRepository userRepository;
  private final ShiftLogRepository shiftLogRepository;
  private final TicketActivityLogRepository activityLogRepository;
  private final AssetRepository assetRepository;

  public TicketController(
          DashboardService dashboardService,
          TicketService ticketService,
          AttachmentService attachmentService,
          TicketCommentService ticketCommentService,
          TicketAssignmentService ticketAssignmentService,
          TicketExportService ticketExportService,
          CurrentUserService currentUserService,
          AppUserRepository userRepository, ShiftLogRepository shiftLogRepository, TicketActivityLogRepository activityLogRepository, AssetRepository assetRepository) {
    this.dashboardService = dashboardService;
    this.ticketService = ticketService;
    this.attachmentService = attachmentService;
    this.ticketCommentService = ticketCommentService;
    this.ticketAssignmentService = ticketAssignmentService;
    this.ticketExportService = ticketExportService;
    this.currentUserService = currentUserService;
    this.userRepository = userRepository;
      this.shiftLogRepository = shiftLogRepository;
      this.activityLogRepository = activityLogRepository;
      this.assetRepository = assetRepository;
  }

  // ============================================
  // DASHBOARD
  // ============================================

  @GetMapping({"/", "/dashboard"})
  public String home(TicketSearchCriteria criteria, Model model) {
    log.info("📊 Loading dashboard with criteria: {}", criteria);

    try {
      // Load dashboard data
      log.info("📈 Loading dashboard view...");
      DashboardView dashboard = dashboardService.loadDashboard(criteria);
      log.info("✅ Dashboard loaded successfully");

      model.addAllAttributes(dashboard.toModel());

      // Add currentUser
      log.info("👤 Getting current user...");
      AppUser currentUser = currentUserService.getCurrentUser();
      if (currentUser == null) {
        log.warn("⚠️ Current user is null - redirecting to login");
        return "redirect:/login";
      }
      model.addAttribute("currentUser", currentUser);
      log.info("✅ Current user: {}", currentUser.getUsername());

      // Load expiring assets
      log.info("🔍 Loading expiring assets...");
      try {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        log.info("📅 Searching for assets expiring between {} and {}", today, futureDate);

        List<Asset> expiringAssets = assetRepository.findAssetsExpiringWithin(today, futureDate);

        int count = expiringAssets != null ? expiringAssets.size() : 0;
        log.info("✅ Found {} expiring assets", count);

        model.addAttribute("expiringAssets", expiringAssets);
        model.addAttribute("expiringCount", count);
      } catch (Exception e) {
        log.error("❌ Failed to load expiring assets: {}", e.getMessage(), e);
        model.addAttribute("expiringAssets", java.util.Collections.emptyList());
        model.addAttribute("expiringCount", 0);
      }

      log.info("✅ Returning index view");
      return "index";

    } catch (Exception e) {
      log.error("❌ CRITICAL ERROR in dashboard: {}", e.getMessage(), e);
      e.printStackTrace(); // This will print full stack trace to console
      return "redirect:/login";
    }
  }
  // ============================================
  // TICKET LIFECYCLE
  // ============================================

  @GetMapping("/tickets/new")
  public String showCreateForm(Model model) {
    model.addAttribute("ticketCreateRequest", new TicketCreateRequest());
    model.addAllAttributes(ticketService.getCreateFormData().toModel());
    return "create-ticket";
  }

  @PostMapping("/tickets")
  public String createTicket(
          @Valid @ModelAttribute TicketCreateRequest request,
          BindingResult result,
          @RequestParam("files") MultipartFile[] files,
          Model model,
          RedirectAttributes redirectAttributes) { // ✅ Added RedirectAttributes

    if (result.hasErrors()) {
      // ✅ FIX: Preserve user input + add fresh dropdowns
      model.addAttribute("ticketCreateRequest", request);
      model.addAllAttributes(ticketService.getCreateFormData().toModel());
      return "create-ticket";
    }

    try {
      Ticket savedTicket = ticketService.createTicket(request, files);
      redirectAttributes.addFlashAttribute("success", "Ticket #" + savedTicket.getId() + " created successfully!");
      return "redirect:/submit-success?ticketId=" + savedTicket.getId();
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
      return "redirect:/tickets/new";
    }
  }

  // Update viewTicketDetails method
  @GetMapping("/tickets/{id}")
  public String viewTicketDetails(@PathVariable Long id, Model model) {
    Ticket ticket = ticketService.getTicketDetails(id);
    model.addAttribute("ticket", ticket);

    List<AppUser> users = userRepository.findAll();
    model.addAttribute("users", users);
    model.addAttribute("newComment", new Comment());

    // ✅ FIXED: Fetch actual ticket activity logs
    List<TicketActivityLog> activityLogs = activityLogRepository.findByTicketIdOrderByCreatedAtDesc(id);
    model.addAttribute("activityLogs", activityLogs);

    return "ticket-details";
  }

  // ✅ FIXED: Changed from GET to POST to prevent accidental state changes
  @PostMapping("/tickets/{id}/resolve")
  public String resolveTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.resolveTicket(id);
      redirectAttributes.addFlashAttribute("success", "Ticket resolved successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to resolve ticket: " + e.getMessage());
    }
    return "redirect:/tickets/" + id;
  }

  // ✅ FIXED: Changed from GET to POST
  @PostMapping("/tickets/{id}/delete")
  public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.deleteTicket(id);
      redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
    }
    return "redirect:/";
  }

  @PostMapping("/tickets/{id}/start-work")
  public String startWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.startWork(id);
      redirectAttributes.addFlashAttribute("success", "Work started on ticket!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to start work: " + e.getMessage());
    }
    return "redirect:/tickets/" + id;
  }

  @PostMapping("/tickets/{id}/complete-work")
  public String completeWork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.completeWork(id);
      redirectAttributes.addFlashAttribute("success", "Work completed on ticket!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to complete work: " + e.getMessage());
    }
    return "redirect:/tickets/" + id;
  }

  // ============================================
  // COMMENTS & ASSIGNMENTS
  // ============================================

  @PostMapping("/tickets/{id}/comments")
  public String addComment(
          @PathVariable Long id,
          @ModelAttribute("newComment") Comment formComment,
          RedirectAttributes redirectAttributes) {
    try {
      ticketCommentService.addComment(id, formComment);
      redirectAttributes.addFlashAttribute("success", "Comment added!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to add comment: " + e.getMessage());
    }
    return "redirect:/tickets/" + id;
  }

  @PostMapping("/tickets/{id}/assign")
  public String assignTicket(
          @PathVariable Long id,
          @RequestParam("userId") Long userId,
          Principal principal,
          RedirectAttributes redirectAttributes) { // ✅ CRITICAL FIX

    try {
      ticketAssignmentService.assignTicket(id, userId, principal.getName());
      redirectAttributes.addFlashAttribute("success", "Ticket assigned successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Assignment failed: " + e.getMessage());
      log.error("Failed to assign ticket {}: {}", id, e.getMessage());
    }

    return "redirect:/tickets/" + id;
  }

  @PostMapping("/tickets/{id}/assign-quick")
  public String quickAssign(
          @PathVariable Long id,
          @RequestParam("userId") Long userId,
          Principal principal,
          RedirectAttributes redirectAttributes) {
    return assignTicket(id, userId, principal, redirectAttributes);
  }

  // ============================================
  // ATTACHMENTS & EXPORT
  // ============================================

  @GetMapping("/tickets/{ticketId}/attachments/{attachmentId}")
  public ResponseEntity<Resource> downloadAttachment(
          @PathVariable Long ticketId, @PathVariable Long attachmentId) {
    Resource resource = attachmentService.downloadAttachmentResource(attachmentId);
    var metadata = attachmentService.getAttachmentMetadata(attachmentId);

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(metadata.getContentType()))
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.getFilename() + "\"")
            .body(resource);
  }

  @GetMapping("/export")
  public void exportTickets(TicketSearchCriteria criteria, HttpServletResponse response)
          throws IOException {
    ticketExportService.exportTickets(criteria, response);
  }

  // ============================================
  // STATIC VIEWS
  // ============================================

  @GetMapping("/submit-success")
  public String submitSuccess(@RequestParam(required = false) Long ticketId, Model model) {
    if (ticketId != null) {
      Ticket ticket = ticketService.getTicketDetails(ticketId);
      model.addAttribute("ticketId", ticket.getId());
      model.addAttribute("ticketPriority", ticket.getPriority().name());
      model.addAttribute(
              "ticketCategory", ticket.getCategory() != null ? ticket.getCategory().getName() : "N/A");
      model.addAttribute(
              "ticketDepartment",
              ticket.getDepartment() != null ? ticket.getDepartment().getName() : "N/A");
      model.addAttribute("ticket", ticket);
      model.addAttribute("responseTimeHours", ticket.getPriority().getSlaHours());

    }
    return "success";
  }

  // ✅ FIXED: Changed from GET to POST
  @PostMapping("/tickets/{id}/unarchive")
  public String unarchiveTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.unarchiveTicket(id);
      redirectAttributes.addFlashAttribute("success", "Ticket unarchived!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to unarchive: " + e.getMessage());
    }
    return "redirect:/";
  }

  // ✅ FIXED: Changed from GET to POST
  @PostMapping("/tickets/{id}/archive")
  public String archiveTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      ticketService.archiveTicket(id);
      redirectAttributes.addFlashAttribute("success", "Ticket archived!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Failed to archive: " + e.getMessage());
    }
    return "redirect:/";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }
}