package com.sujal.itsm.core.offboarding.enums;

public enum OffboardingStatus {
    PENDING,        // Request created, waiting for manager
    IN_PROGRESS,    // Manager approved, IT/HR clearances ongoing
    COMPLETED,      // All clearances done, assets returned
    CANCELLED       // Employee stayed or request voided
}