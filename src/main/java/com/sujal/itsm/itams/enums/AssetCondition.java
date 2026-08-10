package com.sujal.itsm.itams.enums;

public enum AssetCondition {
  NEW("New"),
  GOOD("Good"),
  FAIR("Fair"),
  POOR("Poor"),
  DAMAGED("Damaged");

  private final String displayName;

  AssetCondition(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
