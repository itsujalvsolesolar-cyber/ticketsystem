package com.sujal.itsm.iam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.iam.enums.AccountStatus;
import com.sujal.itsm.iam.enums.EmailProvider;
import com.sujal.itsm.iam.model.EmailAccount;
import com.sujal.itsm.iam.repository.EmailAccountRepository;
import com.sujal.itsm.itams.model.SoftwareLicense;
import com.sujal.itsm.itams.repository.SoftwareLicenseRepository;
import com.sujal.itsm.workflow.enums.ApprovalStatus;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/iam/accounts")
@RequiredArgsConstructor
@Slf4j
public class EmailAccountController {

    private final EmailAccountRepository emailAccountRepository;
    private final AppUserRepository userRepository;
    private final SoftwareLicenseRepository licenseRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    // ✅ REMOVED ObjectMapper from constructor to avoid bean injection issues

    @GetMapping
    public String listEmailAccounts(Model model) {
        List<EmailAccount> accounts = emailAccountRepository.findAllWithUserAndLicense();
        model.addAttribute("accounts", accounts);
        model.addAttribute("pageTitle", "Email Accounts");
        return "iam/account-list";
    }

    @GetMapping("/new")
    public String newEmailAccountForm(Model model) {
        model.addAttribute("emailAccount", new EmailAccount());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("providers", EmailProvider.values());
        model.addAttribute("licenses", licenseRepository.findAll());
        model.addAttribute("pageTitle", "Provision New Email Account");
        return "iam/account-form";
    }

    @PostMapping("/create")
    public String createEmailAccount(
            @RequestParam Long userId,
            @RequestParam String emailAddress,
            @RequestParam EmailProvider provider,
            @RequestParam(required = false) Long licenseId,
            @RequestParam(required = false) String recoveryEmail,
            @RequestParam(required = false) String recoveryPhone,
            RedirectAttributes redirectAttributes) {

        try {
            AppUser requestor = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (emailAccountRepository.existsByEmailAddress(emailAddress)) {
                throw new RuntimeException("Email address already exists.");
            }

            ApprovalRequest approvalRequest = ApprovalRequest.builder()
                    .requestType("EMAIL_PROVISIONING")
                    .requester(requestor)
                    .referenceId(0L)
                    .subject("Email Account Provisioning: " + emailAddress)
                    .description("Request to provision email account for " + requestor.getFullName() +
                            "\nEmail: " + emailAddress +
                            "\nProvider: " + provider +
                            "\nRecovery Email: " + recoveryEmail)
                    .status(ApprovalStatus.PENDING)
                    .moduleType(WorkflowModuleType.EMAIL_PROVISIONING)
                    .currentStep(1)
                    .build();

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("emailAddress", emailAddress);
            requestData.put("provider", provider.name());
            requestData.put("licenseId", licenseId);
            requestData.put("recoveryEmail", recoveryEmail);
            requestData.put("recoveryPhone", recoveryPhone);

            // ✅ Instantiate ObjectMapper locally
            ObjectMapper mapper = new ObjectMapper();
            approvalRequest.setRequestData(mapper.writeValueAsString(requestData));

            approvalRequestRepository.save(approvalRequest);

            log.info("✅ Created approval request for email: {} by user: {}", emailAddress, requestor.getUsername());
            redirectAttributes.addFlashAttribute("success",
                    "Email provisioning request submitted for approval! Request ID: " + approvalRequest.getId());

        } catch (Exception e) {
            log.error("❌ Failed to create email provisioning request", e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit request: " + e.getMessage());
        }

        return "redirect:/iam/accounts";
    }

    @GetMapping("/{id}/edit")
    public String editEmailAccount(@PathVariable Long id, Model model) {
        EmailAccount account = emailAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email account not found"));

        model.addAttribute("account", account);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("providers", EmailProvider.values());
        model.addAttribute("licenses", licenseRepository.findAll());
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("pageTitle", "Edit Email Account: " + account.getEmailAddress());

        return "iam/account-form";
    }

    @PostMapping("/{id}/update")
    public String updateEmailAccount(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String emailAddress,
            @RequestParam EmailProvider provider,
            @RequestParam AccountStatus status,
            @RequestParam(required = false) Long licenseId,
            @RequestParam(required = false) String recoveryEmail,
            @RequestParam(required = false) String recoveryPhone,
            RedirectAttributes redirectAttributes) {

        try {
            EmailAccount account = emailAccountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Email account not found"));

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            account.setUser(user);
            account.setEmailAddress(emailAddress.toLowerCase().trim());
            account.setProvider(provider);
            account.setStatus(status);
            account.setRecoveryEmail(recoveryEmail);
            account.setRecoveryPhone(recoveryPhone);

            if (licenseId != null) {
                SoftwareLicense license = licenseRepository.findById(licenseId)
                        .orElseThrow(() -> new RuntimeException("License not found"));
                account.setLicense(license);
            } else {
                account.setLicense(null);
            }

            if (status == AccountStatus.DISABLED && account.getDisabledAt() == null) {
                account.setDisabledAt(LocalDateTime.now());
            } else if (status != AccountStatus.DISABLED) {
                account.setDisabledAt(null);
            }

            emailAccountRepository.save(account);
            log.info("✅ Updated email account: {}", emailAddress);
            redirectAttributes.addFlashAttribute("success", "Email account updated successfully!");

        } catch (Exception e) {
            log.error("❌ Failed to update email account", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update email: " + e.getMessage());
        }

        return "redirect:/iam/accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteEmailAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            emailAccountRepository.deleteById(id);
            log.info("✅ Deleted email account ID: {}", id);
            redirectAttributes.addFlashAttribute("success", "Email account deleted successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to delete email account", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete email: " + e.getMessage());
        }
        return "redirect:/iam/accounts";
    }
}