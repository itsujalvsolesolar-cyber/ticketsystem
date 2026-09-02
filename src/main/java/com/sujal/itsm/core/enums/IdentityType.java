package com.sujal.itsm.core.enums;

/**
 * Defines the classification of an identity within the ITSM/IAM system.
 * Aligns with enterprise identity governance (NIST / Microsoft Entra ID patterns).
 */
public enum IdentityType {
    HUMAN("Human Employee / User"),
    SHARED_MAILBOX("Shared Mailbox / Group Email"),
    SERVICE_IDENTITY("Service Account / API / System");

    private final String description;

    IdentityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}