package com.sujal.itsm.ticketing.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_activity_logs")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // e.g., CREATED, ASSIGNED, RESOLVED, COMMENTED

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // HTML allowed for bolding names

    @Column(name = "icon_class", length = 50)
    private String iconClass; // e.g., bi-person-check, bi-check-circle

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}