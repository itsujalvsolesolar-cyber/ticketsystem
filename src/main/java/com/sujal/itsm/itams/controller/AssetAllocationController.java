package com.sujal.itsm.itams.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.service.AssetAllocationService;
import com.sujal.itsm.itams.service.AssetService;
import com.sujal.itsm.itams.service.EmployeeService;
import com.sujal.itsm.workflow.enums.SignatureType;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/itams/allocations")
@RequiredArgsConstructor
public class AssetAllocationController {

    private final AssetAllocationService allocationService;
    private final AssetService assetService;
    private final EmployeeService employeeService;

    @GetMapping("/new")
    public String showAllocationForm(@RequestParam("assetId") Long assetId, Model model) {
        Asset asset = assetService.findById(assetId);
        if (asset.getStatus().name().equals("ASSIGNED")) {
            return "redirect:/itams/assets/" + assetId + "?error=already_assigned";
        }
        model.addAttribute("asset", asset);
        model.addAttribute("employees", employeeService.findAllActive());
        model.addAttribute("conditions", com.sujal.itsm.itams.enums.AssetCondition.values());
        model.addAttribute("pageTitle", "Allocate Asset: " + asset.getName());
        return "itams/allocations/new";
    }

    @PostMapping
    public String processAllocation(@RequestParam("assetId") Long assetId,
                                    @RequestParam("employeeId") Long employeeId,
                                    @RequestParam("condition") String condition,
                                    @RequestParam(value = "expectedReturn", required = false) LocalDate expectedReturn,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    @RequestParam(value = "accessories", required = false) List<String> accessories,
                                    @RequestParam(value = "otherAccessory", required = false) String otherAccessory,
                                    RedirectAttributes redirectAttributes) {
        try {
            allocationService.allocateAsset(assetId, employeeId, condition, notes,
                    expectedReturn, buildAccessories(accessories, otherAccessory));
            redirectAttributes.addFlashAttribute("success",
                    "Asset allocated! Employee must now review & accept it.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to allocate asset: " + e.getMessage());
        }
        return "redirect:/itams/assets/" + assetId;
    }

    // ===== Employee Review & Accept page =====
    @GetMapping("/{id}/acceptance")
    public String acceptancePage(@PathVariable Long id, Model model) {
        AssetAllocation allocation = allocationService.findById(id);
        model.addAttribute("allocation", allocation);
        model.addAttribute("pageTitle", "Asset Acceptance – " + allocation.getAsset().getAssetTag());
        return "itams/allocations/acceptance";
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable Long id,
                         @RequestParam String signatureType,
                         @RequestParam(value = "signatureData", required = false) String signatureData,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            allocationService.acceptAllocation(id,
                    SignatureType.valueOf(signatureType.toUpperCase()),
                    signatureData,
                    getClientIp(request),
                    request.getHeader("User-Agent"));
            redirectAttributes.addFlashAttribute("success", "Asset accepted with digital signature.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Acceptance failed: " + e.getMessage());
        }
        return "redirect:/itams/allocations/" + id + "/acceptance";
    }

    @PostMapping("/{id}/return")
    public String processReturn(@PathVariable Long id,
                                @RequestParam("condition") String condition,
                                @RequestParam(value = "notes", required = false) String notes,
                                RedirectAttributes redirectAttributes) {
        try {
            allocationService.returnAsset(id, condition, notes);
            redirectAttributes.addFlashAttribute("success", "Asset returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to return asset: " + e.getMessage());
        }
        return "redirect:/itams/assets";
    }

    private String buildAccessories(List<String> accessories, String other) {
        StringBuilder sb = new StringBuilder();
        if (accessories != null && !accessories.isEmpty()) sb.append(String.join(", ", accessories));
        if (other != null && !other.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(other.trim());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf == null || xf.isBlank()) return request.getRemoteAddr();
        return xf.split(",")[0].trim();
    }
}