package com.sujal.itsm.core.audit.service;

import com.sujal.itsm.core.audit.annotation.Auditable;
import com.sujal.itsm.core.audit.enums.AuditAction;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.model.Ticket;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoftDeleteService {

    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;

    /**
     * Soft deletes a ticket.
     * The @Auditable annotation will automatically log this action to the audit_logs table!
     */
    @Transactional
    @Auditable(action = AuditAction.DELETE, module = "TICKETING", entityType = "Ticket", remarks = "Soft deleted")
    public void softDeleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        AppUser user = currentUserService.getCurrentUser();

        ticket.setDeleted(true);
        ticket.setDeletedAt(LocalDateTime.now());
        ticket.setDeletedBy(user != null ? user.getUsername() : "SYSTEM");

        ticketRepository.save(ticket);
        log.info("🗑️ Ticket {} soft deleted by {}", ticketId, ticket.getDeletedBy());
    }

    /**
     * Restores a soft-deleted ticket.
     */
    @Transactional
    @Auditable(action = AuditAction.RESTORE, module = "TICKETING", entityType = "Ticket", remarks = "Restored from recycle bin")
    public void restoreTicket(Long ticketId) {
        // We MUST use findByIdIncludingDeleted to bypass the @SQLRestriction
        Ticket ticket = ticketRepository.findByIdIncludingDeleted(ticketId)
                .orElseThrow(() -> new RuntimeException("Deleted ticket not found"));

        AppUser user = currentUserService.getCurrentUser();

        ticket.setDeleted(false);
        ticket.setDeletedAt(null);
        ticket.setDeletedBy(null);

        ticketRepository.save(ticket);
        log.info("♻️ Ticket {} restored by {}", ticketId, user != null ? user.getUsername() : "SYSTEM");
    }
}