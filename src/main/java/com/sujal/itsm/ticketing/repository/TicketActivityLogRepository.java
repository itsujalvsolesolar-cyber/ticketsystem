package com.sujal.itsm.ticketing.repository;

import com.sujal.itsm.ticketing.model.TicketActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketActivityLogRepository extends JpaRepository<TicketActivityLog, Long> {
    List<TicketActivityLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}