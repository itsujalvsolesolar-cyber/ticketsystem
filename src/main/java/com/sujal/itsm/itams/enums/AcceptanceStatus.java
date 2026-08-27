package com.sujal.itsm.itams.enums;

public enum AcceptanceStatus {
    PENDING("Pending Acceptance"),
    ACCEPTED("Accepted"),
    WAIVED("Waived by IT");

    private final String displayName;
    AcceptanceStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}