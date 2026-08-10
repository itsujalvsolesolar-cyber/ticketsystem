package com.sujal.itsm.itams.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.model.PurchaseRequest;
import com.sujal.itsm.itams.service.PurchaseRequestService;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.service.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/procurement")
@RequiredArgsConstructor
public class ProcurementController {

    private final PurchaseRequestService prService;
    private final CurrentUserService currentUserService;
    private final WorkflowEngineService workflowEngineService;

    @GetMapping("/requests")
    public String viewRequests(Model model) {
        AppUser currentUser = currentUserService.getCurrentUser();
        model.addAttribute("myRequests", prService.getMyRequests(currentUser.getId()));
        model.addAttribute("pendingRequests", prService.getPendingRequests());
        model.addAttribute("pageTitle", "Purchase Requests");
        return "itams/procurement/requests";
    }

    @GetMapping("/requests/new")
    public String showNewRequestForm(Model model) {
        model.addAttribute("pageTitle", "New Purchase Request");
        return "itams/procurement/new";
    }

    @PostMapping("/requests/create")
    public String createRequest(@RequestParam String title,
                                @RequestParam String justification,
                                @RequestParam BigDecimal estimatedCost,
                                @RequestParam(required = false) String preferredVendor,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            prService.createRequest(currentUser, title, justification, estimatedCost, preferredVendor);
            redirectAttributes.addFlashAttribute("success", "Purchase request submitted for approval!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create request: " + e.getMessage());
        }
        return "redirect:/procurement/requests";
    }

    @GetMapping("/requests/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model) {
        // Reuse the workflow engine to get approval details
        ApprovalRequest approvalRequest = workflowEngineService.getApprovalRequestByReference(id, WorkflowModuleType.PURCHASE_REQUEST)
                .orElseThrow(() -> new RuntimeException("Approval details not found"));

        PurchaseRequest pr = prService.getMyRequests(null).stream() // Simplified lookup for demo
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("PR not found"));

        model.addAttribute("approvalRequest", approvalRequest);
        model.addAttribute("pr", pr);
        model.addAttribute("pageTitle", "PR Details: " + pr.getTitle());

        return "itams/procurement/details";
    }
}