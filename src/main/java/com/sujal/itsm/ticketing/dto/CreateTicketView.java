package com.sujal.itsm.ticketing.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.ticketing.model.Category;
import com.sujal.itsm.ticketing.model.Ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTicketView {
  private Ticket ticket;
  private List<Department> departments;
  private List<Category> categories;

  /** Converts the DTO into a Map for Thymeleaf's model.addAllAttributes() */
  public Map<String, Object> toModel() {
    Map<String, Object> model = new HashMap<>();
    model.put("ticket", ticket);
    model.put("departments", departments);
    model.put("categories", categories);
    return model;
  }
}
