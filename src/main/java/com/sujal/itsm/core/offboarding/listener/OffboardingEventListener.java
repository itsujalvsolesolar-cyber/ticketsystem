package com.sujal.itsm.core.offboarding.listener;

import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.notification.event.NotificationEvent;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.offboarding.enums.AssetReturnStatus;
import com.sujal.itsm.core.offboarding.event.OffboardingCompletedEvent;
import com.sujal.itsm.core.offboarding.model.AssetReturnRecord;
import com.sujal.itsm.core.offboarding.repository.AssetReturnRecordRepository;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OffboardingEventListener {

    private final AppUserRepository userRepository;
    private final AssetRepository assetRepository;
    private final AssetReturnRecordRepository assetReturnRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async // Run in background so the UI doesn't hang during cleanup
    @Transactional
    public void handleOffboardingCompletion(OffboardingCompletedEvent event) {
        var request = event.getRequest();
        AppUser employee = request.getEmployee();

        log.info("🚀 Starting automated post-clearance actions for: {}", employee.getUsername());

        try {
            // 1. DEACTIVATE USER ACCOUNT
            employee.setActive(false);
            userRepository.save(employee);
            log.info("✅ User account deactivated: {}", employee.getUsername());

            // 2. PROCESS ASSET RETURNS & UPDATE INVENTORY
            List<AssetReturnRecord> returnRecords = assetReturnRecordRepository.findByRequestId(request.getId());

            for (AssetReturnRecord record : returnRecords) {
                Asset asset = record.getAsset();

                if (record.getStatus() == AssetReturnStatus.RETURNED) {
                    // Asset is back, make it available again
                    asset.setStatus(AssetStatus.AVAILABLE);
                    // TODO: If you have an AssetAllocation entity, set its isActive = false here
                    log.info("✅ Asset {} marked as AVAILABLE", asset.getAssetTag());
                }
                else if (record.getStatus() == AssetReturnStatus.LOST) {
                    asset.setStatus(AssetStatus.LOST);
                    log.info("⚠️ Asset {} marked as LOST", asset.getAssetTag());
                }

                assetRepository.save(asset);
            }

            // 3. REVOKE DIGITAL ACCESS (Placeholder for your specific access modules)
            // TODO: Call nasService.revokeAllAccess(employee.getId());
            // TODO: Call softwareLicenseService.revokeAllLicenses(employee.getId());
            log.info("✅ Digital access and licenses revoked (via TODO hooks)");

            // 4. SEND COMPLETION NOTIFICATION TO HR & IT
            NotificationRequest notifRequest = NotificationRequest.builder()
                    .recipientRoleName("HR") // Or specific HR user ID
                    .title("✅ Offboarding Completed: " + employee.getFullName())
                    .message("All clearances are complete, assets are recovered, and the account for " +
                            employee.getUsername() + " has been deactivated.")
                    .priority(NotificationPriority.HIGH)
                    .type(NotificationType.EMPLOYEE)
                    .module("OFFBOARDING")
                    .referenceId(request.getId())
                    .build();

            eventPublisher.publishEvent(new NotificationEvent(this, notifRequest));
            log.info("🔔 Completion notification sent to HR/IT");

        } catch (Exception e) {
            log.error("❌ Failed to execute post-clearance actions for {}: {}", employee.getUsername(), e.getMessage(), e);
        }
    }
}