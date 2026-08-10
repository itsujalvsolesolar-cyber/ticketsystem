package com.sujal.itsm.ticketing.dto;

import java.util.List;

import com.sujal.itsm.ticketing.enums.TicketStatus;

import lombok.Data;

@Data
public class TicketSearchCriteria {
  private String status;
  private String priority;
  private Long categoryId;
  private Long departmentId;
  private String search;
  private List<TicketStatus> statuses;

  // Defaults
  private String sortBy = "createdAt";
  private String sortDir = "desc";
  private int page = 0;
  private int size = 10;
}
