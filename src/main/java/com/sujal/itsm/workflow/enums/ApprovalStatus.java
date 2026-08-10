package com.sujal.itsm.workflow.enums;

public enum ApprovalStatus {
    PENDING,       // Workflow created, waiting for first step
    IN_PROGRESS,   // Currently moving through steps
    APPROVED,      // All required steps completed successfully
    REJECTED,      // Rejected at any step
    CANCELLED      // Cancelled by requester or admin
}