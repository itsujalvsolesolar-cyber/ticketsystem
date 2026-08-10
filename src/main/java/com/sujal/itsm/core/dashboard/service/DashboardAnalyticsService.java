package com.sujal.itsm.core.dashboard.service;

import com.sujal.itsm.core.dashboard.dto.ChartDataset;
import com.sujal.itsm.core.dashboard.dto.DashboardKpiResponse;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.repository.AssetRepository;
import com.sujal.itsm.itams.repository.PurchaseRequestRepository;
import com.sujal.itsm.ticketing.repository.TicketRepository;
import com.sujal.itsm.workflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsService {

    private final AssetRepository assetRepository;
    private final TicketRepository ticketRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final CurrentUserService currentUserService;

    public DashboardKpiResponse getExecutiveKpis() {
        AppUser user = currentUserService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);

        // Note: In a real enterprise app, we would pass the user's department ID
        // to these repository methods to enforce row-level security.

        return DashboardKpiResponse.builder()
                .totalAssets(assetRepository.count())
                .availableAssets(assetRepository.countByStatus(com.sujal.itsm.itams.enums.AssetStatus.AVAILABLE))
                .assignedAssets(assetRepository.countByStatus(com.sujal.itsm.itams.enums.AssetStatus.ASSIGNED))
                .expiringAssets30Days(assetRepository.findAssetsExpiringWithin(today, futureDate).size())

                .openTickets(ticketRepository.countByStatus(com.sujal.itsm.ticketing.enums.TicketStatus.OPEN))
                .inProgressTickets(ticketRepository.countByStatus(com.sujal.itsm.ticketing.enums.TicketStatus.IN_PROGRESS))
                .resolvedTickets(ticketRepository.countByStatus(com.sujal.itsm.ticketing.enums.TicketStatus.RESOLVED))

                .pendingApprovals(approvalRequestRepository.countByStatus(com.sujal.itsm.workflow.enums.ApprovalStatus.PENDING))

                .lowStockItems(0) // TODO: Add to ProductRepository
                .pendingPurchaseRequests(purchaseRequestRepository.findByStatus(com.sujal.itsm.itams.model.PurchaseRequest.PRStatus.PENDING_APPROVAL).size())
                .build();
    }

    public ChartDataset getAssetCategoryDistribution() {
        List<Object[]> results = assetRepository.countAssetsByCategory();

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] row : results) {
            labels.add(row[0] != null ? row[0].toString() : "Uncategorized");
            data.add((Long) row[1]);
        }

        return ChartDataset.builder()
                .labels(labels)
                .data(data)
                .build();
    }
}