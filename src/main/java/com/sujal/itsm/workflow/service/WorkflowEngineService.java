package com.sujal.itsm.workflow.service;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.workflow.enums.ApprovalActionType;
import com.sujal.itsm.workflow.enums.ApprovalStatus;
import com.sujal.itsm.workflow.enums.SignatureType;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.event.WorkflowApprovedEvent;
import com.sujal.itsm.workflow.event.WorkflowRejectedEvent;
import com.sujal.itsm.workflow.model.ApprovalAction;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.model.WorkflowDefinition;
import com.sujal.itsm.workflow.model.WorkflowStep;
import com.sujal.itsm.workflow.repository.ApprovalActionRepository;
import com.sujal.itsm.workflow.repository.ApprovalRequestRepository;
import com.sujal.itsm.workflow.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowEngineService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionRepository approvalActionRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Starts a new approval workflow for a specific business entity.
     */
    public ApprovalRequest startWorkflow(Long referenceId, WorkflowModuleType moduleType, AppUser requester) {
        // 1. Find the active workflow template for this module
        WorkflowDefinition definition = workflowDefinitionRepository.findActiveByModuleType(moduleType)
                .orElseThrow(() -> new RuntimeException("No active workflow defined for module: " + moduleType));

        if (definition.getSteps() == null || definition.getSteps().isEmpty()) {
            throw new RuntimeException("Workflow definition has no steps configured.");
        }

        // 2. Create the Approval Request instance
        ApprovalRequest request = ApprovalRequest.builder()
                .referenceId(referenceId)
                .moduleType(moduleType)
                .requester(requester)
                .currentStep(1)
                .status(ApprovalStatus.IN_PROGRESS)
                .build();

        return approvalRequestRepository.save(request);
    }

    /**
     * Helper to get the required role for the current step (useful for UI filtering).
     */
    public String getCurrentRequiredRole(Long approvalRequestId) {
        ApprovalRequest request = approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        WorkflowDefinition definition = workflowDefinitionRepository.findActiveByModuleType(request.getModuleType())
                .orElseThrow(() -> new RuntimeException("Workflow definition not found"));

        return definition.getSteps().stream()
                .filter(step -> step.getStepOrder().equals(request.getCurrentStep()))
                .map(WorkflowStep::getApproverRole)
                .findFirst()
                .orElse("UNKNOWN");
    }

    public ApprovalRequest getPendingApprovalForReference(Long referenceId, WorkflowModuleType moduleType) {
        return approvalRequestRepository.findByReferenceIdAndModuleType(referenceId, moduleType)
                .filter(r -> r.getStatus() == ApprovalStatus.IN_PROGRESS)
                .orElse(null);
    }

    public void processAction(Long approvalRequestId, AppUser approver, ApprovalActionType actionType,
                              String remarks, SignatureType signatureType, String signatureData,
                              String ipAddress, String userAgent) {

        ApprovalRequest request = approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (request.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot process action on a workflow that is not IN_PROGRESS.");
        }

        WorkflowDefinition definition = workflowDefinitionRepository.findActiveByModuleType(request.getModuleType())
                .orElseThrow(() -> new RuntimeException("Workflow definition not found"));

        WorkflowStep currentStep = definition.getSteps().stream()
                .filter(step -> step.getStepOrder().equals(request.getCurrentStep()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current step configuration not found."));

        boolean hasRequiredRole = approver.getRoles().stream()
                .anyMatch(role -> role.getName().equals(currentStep.getApproverRole()));

        if (!hasRequiredRole) {
            throw new RuntimeException("User does not have the required role (" + currentStep.getApproverRole() + ") to approve this step.");
        }

        // ✅ Parse User Agent
        String[] uaInfo = parseUserAgent(userAgent);
        String browser = uaInfo[0];
        String os = uaInfo[1];
        String deviceType = uaInfo[2];

        LocalDateTime signedAt = LocalDateTime.now();

        // ✅ Generate Tamper-Evident Hash
        String hashInput = approver.getId() + "|" + actionType.name() + "|" + signedAt.toString() + "|" + (remarks != null ? remarks : "");
        String hashValue = generateSHA256(hashInput);

        ApprovalAction action = ApprovalAction.builder()
                .approvalRequest(request)
                .stepOrder(request.getCurrentStep())
                .approver(approver)
                .action(actionType)
                .remarks(remarks)
                .signatureType(signatureType != null ? signatureType : SignatureType.ELECTRONIC)
                .signatureData(signatureData)
                .ipAddress(ipAddress)
                .browser(browser)
                .operatingSystem(os)
                .deviceType(deviceType)
                .userAgent(userAgent)
                .hashValue(hashValue)
                .signedAt(signedAt)
                .build();

        approvalActionRepository.save(action);

        if (actionType == ApprovalActionType.REJECTED) {
            request.setStatus(ApprovalStatus.REJECTED);
            approvalRequestRepository.save(request);
            eventPublisher.publishEvent(new WorkflowRejectedEvent(this, request));
            return;
        }

        List<WorkflowStep> sortedSteps = definition.getSteps();
        if (request.getCurrentStep() < sortedSteps.size()) {
            request.setCurrentStep(request.getCurrentStep() + 1);
            approvalRequestRepository.save(request);
        } else {
            request.setStatus(ApprovalStatus.APPROVED);
            approvalRequestRepository.save(request);
            eventPublisher.publishEvent(new WorkflowApprovedEvent(this, request));
        }
    }

    // --- Helper Methods ---

    private String[] parseUserAgent(String userAgent) {
        if (userAgent == null) return new String[]{"Unknown", "Unknown", "Unknown"};
        String ua = userAgent.toLowerCase();

        String browser = "Unknown";
        if (ua.contains("chrome")) browser = "Chrome";
        else if (ua.contains("firefox")) browser = "Firefox";
        else if (ua.contains("safari")) browser = "Safari";
        else if (ua.contains("edg")) browser = "Edge";

        String os = "Unknown";
        if (ua.contains("windows")) os = "Windows";
        else if (ua.contains("mac os")) os = "macOS";
        else if (ua.contains("linux")) os = "Linux";
        else if (ua.contains("android")) os = "Android";
        else if (ua.contains("ios") || ua.contains("iphone")) os = "iOS";

        String device = "Desktop";
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) device = "Mobile";
        else if (ua.contains("tablet") || ua.contains("ipad")) device = "Tablet";

        return new String[]{browser, os, device};
    }

    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "HASH_ERROR";
        }
    }

    public Optional<ApprovalRequest> getApprovalRequestByReference(Long referenceId, WorkflowModuleType moduleType) {
        return approvalRequestRepository.findByReferenceIdAndModuleType(referenceId, moduleType);
    }
}
