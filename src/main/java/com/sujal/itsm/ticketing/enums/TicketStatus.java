package com.sujal.itsm.ticketing.enums;

public enum TicketStatus {
  OPEN("Open"),
  IN_PROGRESS("In Progress"),
  RESOLVED("Resolved"),
  CLOSED("Closed"),
  ARCHIVED("Archived");

  private final String displayName;

  TicketStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
