package com.sujal.itsm.iam.controller;

import com.sujal.itsm.iam.enums.AccountStatus;
import com.sujal.itsm.iam.repository.EmailAccountRepository;
import com.sujal.itsm.itams.repository.SoftwareLicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/iam/dashboard")
@RequiredArgsConstructor
@Slf4j
public class IamDashboardController {

    private final EmailAccountRepository emailAccountRepository;
    private final SoftwareLicenseRepository licenseRepository;

    @GetMapping
    public String showIamDashboard(Model model) {
        log.info("📊 Loading IAM Dashboard...");

        // 1. Email Account KPIs
        long totalAccounts = emailAccountRepository.count();
        long activeAccounts = emailAccountRepository.countByStatus(AccountStatus.ACTIVE);
        long disabledAccounts = emailAccountRepository.countByStatus(AccountStatus.DISABLED);
        long pendingAccounts = emailAccountRepository.countByStatus(AccountStatus.PENDING_PROVISIONING);
        long accountsWithoutMfa = emailAccountRepository.countByIsMfaEnabledFalseAndStatus(AccountStatus.ACTIVE);

        // 2. License KPIs (Assuming you have a SoftwareLicense entity with 'totalSeats' and 'usedSeats')
        // If your license model is different, adjust these queries accordingly.
        long totalLicenses = licenseRepository.count();
        // Example: long availableLicenses = licenseRepository.countAvailableLicenses();
        // For now, we'll use a placeholder or simple count.
        long availableLicenses = Math.max(0, totalLicenses - activeAccounts); // Simplified logic

        // 3. Add to Model
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("activeAccounts", activeAccounts);
        model.addAttribute("disabledAccounts", disabledAccounts);
        model.addAttribute("pendingAccounts", pendingAccounts);
        model.addAttribute("accountsWithoutMfa", accountsWithoutMfa);
        model.addAttribute("totalLicenses", totalLicenses);
        model.addAttribute("availableLicenses", availableLicenses);
        model.addAttribute("pageTitle", "IAM Dashboard");

        return "iam/dashboard";
    }
}