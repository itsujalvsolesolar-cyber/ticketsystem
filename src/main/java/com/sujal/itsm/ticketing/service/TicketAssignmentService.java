package com.sujal.itsm.ticketing.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.notification.event.NotificationEvent;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.ticketing.exception.TicketNotFoundException;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.model.TicketActivityLog;
import com.sujal.itsm.ticketing.repository.TicketActivityLogRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import com.sujal.itsm.core.email.service.EmailService;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TicketAssignmentService {

  private final TicketRepository ticketRepository;
  private final AppUserRepository appUserRepository;
  private final EmailService emailService;
  private final TicketActivityLogRepository activityLogRepository;
  private final ApplicationEventPublisher eventPublisher; // ✅ NEW: Event Publisher

  public void assignTicket(Long ticketId, Long userId, String adminName) {
    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));

    if (userId == 0) {
      ticket.setAssignedTo(null);
      ticket.setAssignedAt(null);

      TicketActivityLog activityLog = TicketActivityLog.builder()
              .ticket(ticket)
              .actionType("UNASSIGNED")
              .message("Unassigned by <strong>" + adminName + "</strong>")
              .iconClass("bi-person-x")
              .build();
      activityLogRepository.save(activityLog);

    } else {
      AppUser user = appUserRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + userId));

      ticket.setAssignedTo(user);
      ticket.setAssignedAt(LocalDateTime.now());
      ticketRepository.save(ticket);

      TicketActivityLog activityLog = TicketActivityLog.builder()
              .ticket(ticket)
              .actionType("ASSIGNED")
              .message("Assigned to <strong>" + user.getUsername() + "</strong> by " + adminName)
              .iconClass("bi-person-check")
              .build();
      activityLogRepository.save(activityLog);

      // ✅ NEW: Event-Driven Notification + Email
      try {
        String recipientEmail = user.getEmail() != null
                ? user.getEmail()
                : user.getUsername() + "@company.com";

        // ✅ NEW: Use the centralized EmailService with our HTML template
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("assigneeName", user.getFullName() != null ? user.getFullName() : user.getUsername());
        emailData.put("ticketTitle", ticket.getTitle());
        emailData.put("priority", ticket.getPriority() != null ? ticket.getPriority().name() : "Medium");
        emailData.put("category", ticket.getCategory() != null ? ticket.getCategory().getName() : "General");
        emailData.put("assignedBy", adminName);
        emailData.put("ticketUrl", "http://localhost:9090/tickets/" + ticket.getId());

        emailService.sendHtmlEmail(
                recipientEmail,
                "New Ticket Assigned: " + ticket.getTitle(),
                "emails/ticket-assignment",
                emailData
        );

        NotificationRequest notifRequest = NotificationRequest.builder()
                .recipientUserId(user.getId())
                .title("🎟️ Ticket Assigned to You")
                .message("You have been assigned to ticket: " + ticket.getTitle())
                .priority(NotificationPriority.HIGH)
                .type(NotificationType.TICKET)
                .module("TICKETING")
                .referenceId(ticket.getId())
                .build();
        eventPublisher.publishEvent(new NotificationEvent(this, notifRequest));

      } catch (Exception e) {
        log.error("Failed to send notifications for ticket {}: {}", ticket.getId(), e.getMessage());
      }
    }
  }
}