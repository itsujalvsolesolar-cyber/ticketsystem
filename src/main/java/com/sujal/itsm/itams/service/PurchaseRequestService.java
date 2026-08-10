package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.model.PurchaseRequest;
import com.sujal.itsm.itams.repository.PurchaseRequestRepository;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.event.WorkflowApprovedEvent;
import com.sujal.itsm.workflow.event.WorkflowRejectedEvent;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.service.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseRequestService {

    private final PurchaseRequestRepository prRepository;
    private final WorkflowEngineService workflowEngineService;

    public PurchaseRequest createRequest(AppUser requester, String title, String justification,
                                         BigDecimal estimatedCost, String preferredVendor) {

        PurchaseRequest pr = PurchaseRequest.builder()
                .requester(requester)
                .title(title)
                .justification(justification)
                .estimatedCost(estimatedCost)
                .preferredVendor(preferredVendor)
                .status(PurchaseRequest.PRStatus.PENDING_APPROVAL)
                .build();

        PurchaseRequest savedPr = prRepository.save(pr);

        // 🚀 Trigger the Enterprise Approval Engine!
        ApprovalRequest approval = workflowEngineService.startWorkflow(
                savedPr.getId(),
                WorkflowModuleType.PURCHASE_REQUEST,
                requester
        );

        // Link them together
        savedPr.setApprovalRequestId(approval.getId());
        return prRepository.save(savedPr);
    }

    public List<PurchaseRequest> getPendingRequests() {
        return prRepository.findByStatus(PurchaseRequest.PRStatus.PENDING_APPROVAL);
    }

    public List<PurchaseRequest> getMyRequests(Long userId) {
        return prRepository.findByRequesterId(userId);
    }

    // ✅ Listen for Approval Engine Events to update the PR status automatically
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePrApproved(WorkflowApprovedEvent event) {
        if (event.getApprovalRequest().getModuleType() == WorkflowModuleType.PURCHASE_REQUEST) {
            PurchaseRequest pr = prRepository.findById(event.getApprovalRequest().getReferenceId())
                    .orElseThrow(() -> new RuntimeException("PR not found"));
            pr.setStatus(PurchaseRequest.PRStatus.APPROVED);
            prRepository.save(pr);
            // Future: Auto-create a procurement task or notify the purchasing team
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePrRejected(WorkflowRejectedEvent event) {
        if (event.getApprovalRequest().getModuleType() == WorkflowModuleType.PURCHASE_REQUEST) {
            PurchaseRequest pr = prRepository.findById(event.getApprovalRequest().getReferenceId())
                    .orElseThrow(() -> new RuntimeException("PR not found"));
            pr.setStatus(PurchaseRequest.PRStatus.REJECTED);
            prRepository.save(pr);
        }
    }
}