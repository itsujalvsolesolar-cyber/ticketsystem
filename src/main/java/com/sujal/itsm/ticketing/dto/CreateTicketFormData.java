package com.sujal.itsm.ticketing.dto;

import java.util.List;

import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.model.Ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTicketFormData {
  private Ticket ticket;
  private List<Department> departments;
  private List<Category> categories;
}
