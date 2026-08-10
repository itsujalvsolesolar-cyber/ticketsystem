package com.sujal.itsm.workflow.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.workflow.enums.ApprovalStatus;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.repository.ApprovalRequestRepository;
import com.sujal.itsm.workflow.event.WorkflowApprovedEvent;
import com.sujal.itsm.workflow.event.WorkflowRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/approvals")
@RequiredArgsConstructor
@Slf4j
public class ApprovalRequestController {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping
    public String listApprovalRequests(Model model) {
        model.addAttribute("requests", approvalRequestRepository.findAll());
        model.addAttribute("pageTitle", "Approval Requests");
        return "workflow/approval-requests";
    }

    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam(required = false) String comments,
                                 RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            ApprovalRequest request = approvalRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Approval request not found"));

            // 1. Update status to APPROVED
            request.setStatus(ApprovalStatus.APPROVED);

            // 2. If your ApprovalRequest entity has an 'approvedBy' field, uncomment this:
            // request.setApprovedBy(currentUser);

            approvalRequestRepository.save(request);

            // 3. Publish event to trigger EmailProvisioningApprovalListener (passing 'this' as source)
            eventPublisher.publishEvent(new WorkflowApprovedEvent(this, request));

            log.info("✅ Approved request {} by user {}", id, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Request approved successfully!");

        } catch (Exception e) {
            log.error("❌ Failed to approve request", e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve: " + e.getMessage());
        }

        return "redirect:/approvals";
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false) String comments,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            ApprovalRequest request = approvalRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Approval request not found"));

            // 1. Update status to REJECTED
            request.setStatus(ApprovalStatus.REJECTED);
            approvalRequestRepository.save(request);

            // 2. Publish rejection event (passing 'this' as source)
            eventPublisher.publishEvent(new WorkflowRejectedEvent(this, request));

            log.info("✅ Rejected request {} by user {}", id, currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Request rejected.");

        } catch (Exception e) {
            log.error("❌ Failed to reject request", e);
            redirectAttributes.addFlashAttribute("error", "Failed to reject: " + e.getMessage());
        }

        return "redirect:/approvals";
    }
}