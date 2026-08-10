package com.sujal.itsm.core.enums;

public enum UserRole {
  ADMIN,
  MANAGER,
  AGENT;

  // Helper for Spring Security
  public String getAuthority() {
    return "ROLE_" + this.name();
  }
}
