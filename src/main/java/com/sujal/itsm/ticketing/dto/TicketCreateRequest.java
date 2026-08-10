package com.sujal.itsm.ticketing.dto;

import com.sujal.itsm.ticketing.enums.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateRequest {

  @NotBlank(message = "Requester name is required")
  @Size(max = 100)
  private String requesterName;

  @NotBlank(message = "Title is required")
  @Size(min = 5, max = 200)
  private String title;

  @Size(max = 5000)
  private String description;

  @Size(max = 50)
  private String intercomNumber;

  @NotNull(message = "Priority is required")
  private TicketPriority priority;

  @NotNull(message = "Category is required")
  private Long categoryId;

  @NotNull(message = "Department is required")
  private Long departmentId;
}
