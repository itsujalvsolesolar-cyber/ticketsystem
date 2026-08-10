package com.sujal.itsm.itams.enums;

public enum AccessType {
    AD_ACCOUNT("Active Directory Account"),
    VPN("VPN Access"),
    NAS("NAS / Shared Folder"),
    FIREWALL("Firewall Rule"),
    PRINTER("Network Printer"),
    WIFI("Wi-Fi Credentials"),
    EMAIL("Email Account"),
    OTHER("Other");

    private final String displayName;

    AccessType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}