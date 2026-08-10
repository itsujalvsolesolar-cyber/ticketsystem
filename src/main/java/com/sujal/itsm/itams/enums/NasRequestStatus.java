package com.sujal.itsm.itams.enums;

public enum NasRequestStatus {
    PENDING_IT,       // Waiting for IT Executive approval
    PENDING_MD,       // Waiting for MD/CEO approval (for restricted folders)
    APPROVED,         // Fully approved and provisioned
    REJECTED,         // Denied by approver
    REVOKED           // Access removed (can be silent)
}