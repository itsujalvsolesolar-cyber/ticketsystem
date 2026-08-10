package com.sujal.itsm.itams.enums;

public enum AssetStatus {
  AVAILABLE("Available"),
  ASSIGNED("Assigned"),
  IN_REPAIR("In Repair"),
  RETIRED("Retired"),
  SCRAPPED("Scrapped"),
  LOST("Lost");

  private final String displayName;

  AssetStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
