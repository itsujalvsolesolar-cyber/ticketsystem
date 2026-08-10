package com.sujal.itsm.iam.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.iam.enums.AccountStatus;
import com.sujal.itsm.iam.enums.EmailProvider;
import com.sujal.itsm.iam.model.EmailAccount;
import com.sujal.itsm.iam.repository.EmailAccountRepository;
import com.sujal.itsm.itams.model.SoftwareLicense;
import com.sujal.itsm.itams.repository.SoftwareLicenseRepository;
import com.sujal.itsm.workflow.event.WorkflowApprovedEvent;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvisioningApprovalListener {

    private final EmailAccountRepository emailAccountRepository;
    private final AppUserRepository userRepository;
    private final SoftwareLicenseRepository licenseRepository;
    // ✅ REMOVED ObjectMapper from constructor

    @EventListener
    public void onEmailProvisioningApproved(WorkflowApprovedEvent event) {
        ApprovalRequest approvalRequest = event.getApprovalRequest();

        if (!"EMAIL_PROVISIONING".equals(approvalRequest.getRequestType())) {
            return;
        }

        try {
            // ✅ Instantiate ObjectMapper locally
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestData = objectMapper.readValue(
                    approvalRequest.getRequestData(), Map.class);

            Long userId = Long.valueOf(requestData.get("userId").toString());
            String emailAddress = (String) requestData.get("emailAddress");
            EmailProvider provider = EmailProvider.valueOf((String) requestData.get("provider"));
            Long licenseId = requestData.get("licenseId") != null ?
                    Long.valueOf(requestData.get("licenseId").toString()) : null;
            String recoveryEmail = (String) requestData.getOrDefault("recoveryEmail", "");
            String recoveryPhone = (String) requestData.getOrDefault("recoveryPhone", "");

            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String provisionedBy = "SYSTEM";
            if (approvalRequest.getApprovedBy() != null) {
                provisionedBy = approvalRequest.getApprovedBy().getUsername();
            }

            EmailAccount emailAccount = EmailAccount.builder()
                    .user(user)
                    .emailAddress(emailAddress.toLowerCase().trim())
                    .provider(provider)
                    .status(AccountStatus.ACTIVE)
                    .recoveryEmail(recoveryEmail)
                    .recoveryPhone(recoveryPhone)
                    .provisionedBy(provisionedBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            if (licenseId != null) {
                SoftwareLicense license = licenseRepository.findById(licenseId)
                        .orElseThrow(() -> new RuntimeException("License not found"));
                emailAccount.setLicense(license);
            }

            emailAccountRepository.save(emailAccount);
            log.info("✅ Auto-provisioned email account {} after approval", emailAddress);

        } catch (Exception e) {
            log.error("❌ Failed to auto-provision email after approval", e);
        }
    }
}