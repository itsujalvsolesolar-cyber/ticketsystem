package com.sujal.itsm.ticketing.enums;

public enum TicketPriority {
  LOW("Low", 24),
  MEDIUM("Medium", 8),
  HIGH("High", 4),
  CRITICAL("Critical", 2);

  private final String displayName;
  private final int slaHours;

  TicketPriority(String displayName, int slaHours) {
    this.displayName = displayName;
    this.slaHours = slaHours;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getSlaHours() {
    return slaHours;
  }
}
