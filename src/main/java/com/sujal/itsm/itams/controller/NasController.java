package com.sujal.itsm.itams.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.NasPermissionLevel;
import com.sujal.itsm.itams.model.NasAccessRequest;
import com.sujal.itsm.itams.model.NasFolder;
import com.sujal.itsm.itams.repository.NasAccessRequestRepository;
import com.sujal.itsm.itams.service.NasService;
import com.sujal.itsm.workflow.enums.ApprovalActionType;
import com.sujal.itsm.workflow.enums.SignatureType;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.service.PdfCertificateService;
import com.sujal.itsm.workflow.service.WorkflowEngineService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/nas")
@RequiredArgsConstructor
public class NasController {

    private final NasService nasService;
    private final CurrentUserService currentUserService;
    private final WorkflowEngineService workflowEngineService;
    private final HttpServletRequest request;
    private final PdfCertificateService pdfCertificateService;
    private final NasAccessRequestRepository nasAccessRequestRepository;

    @GetMapping("/folders")
    public String viewFolders(Model model) {
        List<NasFolder> folders = nasService.getAllFolders();
        model.addAttribute("folders", folders);
        model.addAttribute("pageTitle", "NAS Folder Management");
        return "itams/nas/folders";
    }

    @PostMapping("/folders/create")
    public String createFolder(@RequestParam String folderName,
                               @RequestParam String networkPath,
                               @RequestParam(required = false) String description,
                               @RequestParam String sensitivityLevel,
                               @RequestParam(defaultValue = "false") boolean requiresExecutiveApproval,
                               RedirectAttributes redirectAttributes) {
        try {
            nasService.createFolder(folderName, networkPath, description, sensitivityLevel, requiresExecutiveApproval);
            redirectAttributes.addFlashAttribute("success", "NAS Folder created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create folder: " + e.getMessage());
        }
        return "redirect:/nas/folders";
    }

    @GetMapping("/requests")
    public String viewRequests(Model model) {
        AppUser currentUser = currentUserService.getCurrentUser();
        model.addAttribute("pendingIt", nasService.getPendingItRequests());
        model.addAttribute("pendingMd", nasService.getPendingMdRequests());
        model.addAttribute("myApproved", nasService.getApprovedRequestsForEmployee(currentUser.getId()));
        model.addAttribute("pageTitle", "NAS Access Requests");
        return "itams/nas/requests";
    }

    // ✅ FIXED: Added signatureType and signatureData parameters
    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam String signatureType,
                                 @RequestParam String signatureData,
                                 @RequestParam(required = false) String remarks,
                                 RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            ApprovalRequest approval = workflowEngineService.getPendingApprovalForReference(id, WorkflowModuleType.NAS_ACCESS);

            if (approval == null) {
                throw new RuntimeException("No pending workflow found for this request.");
            }

            workflowEngineService.processAction(
                    approval.getId(),
                    currentUser,
                    ApprovalActionType.APPROVED,
                    remarks,
                    SignatureType.valueOf(signatureType.toUpperCase()),
                    signatureData,
                    getClientIp(request),
                    request.getHeader("User-Agent")
            );
            redirectAttributes.addFlashAttribute("success", "Request approved and signed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve: " + e.getMessage());
        }
        return "redirect:/nas/requests";
    }

    // ✅ FIXED: Added signatureType and signatureData parameters
    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam String signatureType,
                                @RequestParam String signatureData,
                                @RequestParam(required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            ApprovalRequest approval = workflowEngineService.getPendingApprovalForReference(id, WorkflowModuleType.NAS_ACCESS);

            if (approval == null) {
                throw new RuntimeException("No pending workflow found for this request.");
            }

            workflowEngineService.processAction(
                    approval.getId(),
                    currentUser,
                    ApprovalActionType.REJECTED,
                    remarks != null ? remarks : "Rejected by " + currentUser.getUsername(),
                    SignatureType.valueOf(signatureType.toUpperCase()),
                    signatureData,
                    getClientIp(request),
                    request.getHeader("User-Agent")
            );
            redirectAttributes.addFlashAttribute("success", "Request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject: " + e.getMessage());
        }
        return "redirect:/nas/requests";
    }

    @PostMapping("/requests/{id}/revoke")
    public String revoke(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean notify, RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            nasService.revokeAccess(id, currentUser.getId(), notify);
            redirectAttributes.addFlashAttribute("success", "Access revoked successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to revoke: " + e.getMessage());
        }
        return "redirect:/nas/requests";
    }

    @PostMapping("/folders/{id}/request")
    public String requestAccess(@PathVariable Long id,
                                @RequestParam NasPermissionLevel permissionLevel,
                                @RequestParam(required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser currentUser = currentUserService.getCurrentUser();
            nasService.requestAccess(id, currentUser.getId(), currentUser.getId(), permissionLevel, remarks);
            redirectAttributes.addFlashAttribute("success", "Access request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit request: " + e.getMessage());
        }
        return "redirect:/nas/folders";
    }

    @GetMapping("/requests/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model) {
        ApprovalRequest approvalRequest = workflowEngineService.getApprovalRequestByReference(id, WorkflowModuleType.NAS_ACCESS)
                .orElseThrow(() -> new RuntimeException("Approval details not found"));

        NasAccessRequest nasRequest = nasAccessRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NAS Request not found"));

        model.addAttribute("approvalRequest", approvalRequest);
        model.addAttribute("nasRequest", nasRequest);
        model.addAttribute("pageTitle", "Request Details: " + nasRequest.getFolder().getFolderName());
        return "itams/nas/details";
    }

    @GetMapping("/requests/{id}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        try {
            ApprovalRequest req = workflowEngineService.getApprovalRequestByReference(id, WorkflowModuleType.NAS_ACCESS)
                    .orElseThrow(() -> new RuntimeException("Approval details not found"));

            byte[] pdfBytes = pdfCertificateService.generateApprovalCertificate(req);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Approval_Certificate_NAS_" + id + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) return request.getRemoteAddr();
        return xfHeader.split(",")[0];
    }
}