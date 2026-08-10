package com.sujal.itsm.core.offboarding.controller;

import com.sujal.itsm.core.offboarding.dto.OffboardingInitiationRequest;
import com.sujal.itsm.core.offboarding.model.OffboardingRequest;
import com.sujal.itsm.core.offboarding.repository.OffboardingRequestRepository;
import com.sujal.itsm.core.offboarding.service.OffboardingService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/offboarding")
@RequiredArgsConstructor
public class OffboardingUiController {

    private final OffboardingRequestRepository offboardingRepo;
    private final OffboardingService offboardingService;
    private final AppUserRepository userRepo;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("pendingRequests", offboardingRepo.findByStatus(com.sujal.itsm.core.offboarding.enums.OffboardingStatus.IN_PROGRESS));
        model.addAttribute("completedRequests", offboardingRepo.findByStatus(com.sujal.itsm.core.offboarding.enums.OffboardingStatus.COMPLETED));
        model.addAttribute("allEmployees", userRepo.findByIsActiveTrue()); // For the dropdown
        model.addAttribute("pageTitle", "Offboarding Dashboard");
        return "offboarding/dashboard";
    }

    // ✅ ADDED: Handle the form submission
    @PostMapping("/initiate")
    public String initiateOffboarding(
            @RequestParam Long employeeId,
            @RequestParam Long managerId,
            @RequestParam LocalDate lastWorkingDay,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {
            OffboardingInitiationRequest request = new OffboardingInitiationRequest();
            request.setEmployeeId(employeeId);
            request.setManagerId(managerId);
            request.setLastWorkingDay(lastWorkingDay);
            request.setReason(reason);
            request.setResignationDate(LocalDate.now());

            offboardingService.initiateOffboarding(request);
            redirectAttributes.addFlashAttribute("success", "Offboarding initiated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to initiate: " + e.getMessage());
        }
        return "redirect:/offboarding/dashboard";
    }

    @GetMapping("/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        OffboardingRequest request = offboardingService.getOffboardingDetails(id);
        model.addAttribute("request", request);
        model.addAttribute("clearances", offboardingService.getClearancesForRequest(id));
        model.addAttribute("assetReturns", offboardingService.getAssetReturnsForRequest(id));
        model.addAttribute("pageTitle", "Offboarding Details: " + request.getEmployee().getFullName());
        return "offboarding/details";
    }
}