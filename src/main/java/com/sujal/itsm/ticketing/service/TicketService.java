package com.sujal.itsm.ticketing.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sujal.itsm.core.audit.annotation.Auditable;
import com.sujal.itsm.core.audit.enums.AuditAction;
import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.notification.event.NotificationEvent;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.ticketing.dto.CreateTicketView;
import com.sujal.itsm.ticketing.dto.TicketCreateRequest;
import com.sujal.itsm.ticketing.enums.TicketStatus;
import com.sujal.itsm.ticketing.exception.TicketNotFoundException;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.CategoryRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService {

  private final TicketRepository ticketRepository;
  private final DepartmentRepository departmentRepository;
  private final CategoryRepository categoryRepository;
  private final AppUserRepository appUserRepository;
  private final AttachmentService attachmentService;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher eventPublisher;

  private Ticket requireTicket(Long id) {
    return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
  }

  public CreateTicketView getCreateFormData() {
    return CreateTicketView.builder()
            .ticket(new Ticket())
            .departments(departmentRepository.findAll())
            .categories(categoryRepository.findAll())
            .build();
  }

  // ✅ THIS IS THE ONLY createTicket METHOD, WITH THE AUDIT ANNOTATION
  @Auditable(action = AuditAction.CREATE, module = "TICKETING", entityType = "Ticket", remarks = "New ticket created via UI")
  public Ticket createTicket(TicketCreateRequest request, MultipartFile[] files) {
    Ticket ticket = Ticket.builder()
            .requesterName(request.getRequesterName())
            .title(request.getTitle())
            .description(request.getDescription())
            .intercomNumber(request.getIntercomNumber())
            .priority(request.getPriority())
            .status(TicketStatus.OPEN)
            .department(departmentRepository.findById(request.getDepartmentId()).orElse(null))
            .category(categoryRepository.findById(request.getCategoryId()).orElse(null))
            .build();

    Ticket savedTicket = ticketRepository.save(ticket);

    // Event-Driven Notification for Admins
    if (savedTicket.getDepartment() != null) {
      appUserRepository.findAll().stream()
              .filter(u -> u.isAdmin() || u.hasRole("IT MANAGER") || u.hasRole("DEPARTMENT HEAD"))
              .forEach(user -> {
                NotificationRequest notifRequest = NotificationRequest.builder()
                        .recipientUserId(user.getId())
                        .title("🎟️ New Ticket Created")
                        .message(savedTicket.getTitle() + " by " + savedTicket.getRequesterName())
                        .priority(NotificationPriority.MEDIUM)
                        .type(NotificationType.TICKET)
                        .module("TICKETING")
                        .referenceId(savedTicket.getId())
                        .build();
                eventPublisher.publishEvent(new NotificationEvent(this, notifRequest));
              });
    }

    if (files != null && files.length > 0) {
      attachmentService.uploadAttachments(savedTicket, files);
    }

    return savedTicket;
  }

  @Transactional(readOnly = true)
  public Ticket getTicketDetails(Long id) {
    Ticket ticket = ticketRepository.findByIdWithAttachments(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    ticket.getComments().size(); // Force initialization
    return ticket;
  }

  public void archiveTicket(Long id) {
    Ticket ticket = requireTicket(id);
    ticket.setStatus(TicketStatus.ARCHIVED);
  }

  public void resolveTicket(Long id) {
    Ticket ticket = requireTicket(id);
    ticket.setStatus(TicketStatus.RESOLVED);
    ticket.setResolvedAt(LocalDateTime.now());
  }

  public void deleteTicket(Long id) {
    if (!ticketRepository.existsById(id)) throw new TicketNotFoundException(id);
    ticketRepository.deleteById(id);
  }

  public void startWork(Long id) {
    Ticket ticket = requireTicket(id);
    ticket.setStartedAt(LocalDateTime.now());

    AppUser currentUser = currentUserService.getCurrentUser();
    if (ticket.getAssignedTo() == null && currentUser != null) {
      ticket.setAssignedTo(currentUser);
      ticket.setAssignedAt(LocalDateTime.now());
    }
  }

  public void unarchiveTicket(Long id) {
    Ticket ticket = requireTicket(id);
    ticket.setStatus(TicketStatus.OPEN);
  }

  public void completeWork(Long id) {
    Ticket ticket = requireTicket(id);
    ticket.setCompletedAt(LocalDateTime.now());
    ticket.setStatus(TicketStatus.RESOLVED);
    if (ticket.getResolvedAt() == null) {
      ticket.setResolvedAt(LocalDateTime.now());
    }
  }
}