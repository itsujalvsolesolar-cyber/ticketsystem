package com.sujal.itsm.core.notification.scheduler;

import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.notification.event.NotificationEvent;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final AssetRepository assetRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ✅ Runs every day at 8:00 AM
//    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkExpiringAssets() {
        log.info("🕒 Running scheduled job: Checking for expiring assets...");

        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);

        List<Asset> expiringAssets = assetRepository.findAssetsExpiringWithin(today, futureDate);

        if (expiringAssets.isEmpty()) {
            log.info("✅ No assets expiring in the next 30 days.");
            return;
        }

        log.info("⚠️ Found {} assets expiring in the next 30 days.", expiringAssets.size());

        // Find all IT Managers or Admins to notify
        List<AppUser> admins = appUserRepository.findAll().stream()
                .filter(u -> u.isAdmin() || u.hasRole("IT MANAGER") || u.hasRole("SUPER_ADMIN"))
                .toList();

        for (Asset asset : expiringAssets) {
            StringBuilder reason = new StringBuilder();
            if (asset.getWarrantyEndDate() != null && !asset.getWarrantyEndDate().isAfter(futureDate)) {
                reason.append("Warranty expires on ").append(asset.getWarrantyEndDate()).append(". ");
            }
            if (asset.getAmcEndDate() != null && !asset.getAmcEndDate().isAfter(futureDate)) {
                reason.append("AMC expires on ").append(asset.getAmcEndDate()).append(". ");
            }

            for (AppUser admin : admins) {
                NotificationRequest request = NotificationRequest.builder()
                        .recipientUserId(admin.getId())
                        .title("⚠️ Asset Expiring Soon")
                        .message("Asset '" + asset.getName() + "' (" + asset.getAssetTag() + ") requires attention. " + reason.toString())
                        .priority(NotificationPriority.HIGH)
                        .type(NotificationType.ASSET)
                        .module("ITAMS")
                        .referenceId(asset.getId())
                        .build();

                eventPublisher.publishEvent(new NotificationEvent(this, request));
            }
        }

        log.info("✅ Scheduled job completed. Notifications dispatched.");
    }
}