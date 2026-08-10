package com.sujal.itsm.core.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiResponse {
    // Asset KPIs
    private long totalAssets;
    private long availableAssets;
    private long assignedAssets;
    private long expiringAssets30Days;

    // Ticket KPIs
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;

    // Approval KPIs
    private long pendingApprovals;
    private long approvedThisMonth;

    // Inventory KPIs
    private long lowStockItems;
    private long pendingPurchaseRequests;
}