package com.sujal.itsm.employee.controller;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeAcceptanceController {

    private final AssetAllocationRepository allocationRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/acceptances")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'STAFF')")
    public String pendingAcceptances(Model model) {
        Long userId = currentUserService.getCurrentUser().getId();
        // Fetch allocations linked to this user's employee profile that are PENDING
        List<AssetAllocation> pending = allocationRepository.findPendingByUserId(userId);
        model.addAttribute("pendingAllocations", pending);
        return "employee/acceptances";
    }

    @PostMapping("/allocations/{id}/accept")
    @ResponseBody
    @PreAuthorize("@securityEvaluator.isAllocationOwner(authentication, #id)") // Pillar 2 Security
    public ResponseEntity<?> signAndAccept(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload, 
            HttpServletRequest request) {
        
        AssetAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));

        String signatureBase64 = payload.get("signature");
        if (signatureBase64 == null || signatureBase64.isBlank() || signatureBase64.length() < 100) {
            return ResponseEntity.badRequest().body("Invalid signature data.");
        }

        // Forensic Audit Trail
        allocation.setDigitalSignature(signatureBase64);
        allocation.setAcceptanceStatus(com.sujal.itsm.itams.enums.AcceptanceStatus.ACCEPTED);
        allocation.setAcceptedAt(LocalDateTime.now());
        allocation.setAcceptedIp(getClientIp(request));
        allocation.setAcceptedUserAgent(request.getHeader("User-Agent"));

        allocationRepository.save(allocation);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Asset accepted and signed."));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}