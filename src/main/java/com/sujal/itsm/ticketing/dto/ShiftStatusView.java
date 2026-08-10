package com.sujal.itsm.ticketing.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.ticketing.model.ShiftLog;

import lombok.Builder;
import lombok.Data;

/** View DTO for the Shift Status page. Encapsulates all data needed to render the page. */
@Data
@Builder
public class ShiftStatusView {
  private AppUser currentUser;
  private ShiftLog activeShift;
  private List<ShiftLog> recentShifts;

  /** Converts this DTO into a Map for Thymeleaf's model.addAllAttributes(). */
  public Map<String, Object> toModel() {
    Map<String, Object> model = new HashMap<>();
    model.put("currentUser", currentUser);
    model.put("activeShift", activeShift);
    model.put("recentShifts", recentShifts);
    return model;
  }
}
