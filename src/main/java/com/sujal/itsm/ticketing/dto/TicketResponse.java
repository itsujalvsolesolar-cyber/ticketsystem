package com.sujal.itsm.ticketing.dto;

import java.time.LocalDateTime;

import com.sujal.itsm.ticketing.enums.TicketPriority;
import com.sujal.itsm.ticketing.enums.TicketStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponse {
  private Long id;
  private String title;
  private String requesterName;
  private TicketStatus status;
  private TicketPriority priority;
  private String departmentName;
  private String categoryName;
  private String assignedToUsername;
  private LocalDateTime createdAt;
  private String slaStatus;
}
