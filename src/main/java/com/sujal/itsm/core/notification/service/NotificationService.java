package com.sujal.itsm.core.notification.service;

import com.sujal.itsm.core.email.service.EmailService;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.notification.model.Notification;
import com.sujal.itsm.core.notification.repository.NotificationRepository;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    private final AssetRepository assetRepository;

    @Transactional
    public void createAndDispatch(NotificationRequest request) {
        List<AppUser> recipients = findRecipients(request);

        for (AppUser user : recipients) {
            // 1. Save to Database
            Notification notification = Notification.builder()
                    .user(user)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .priority(request.getPriority())
                    .type(request.getType())
                    .module(request.getModule())
                    .referenceId(request.getReferenceId())
                    .build();

            notificationRepository.save(notification);

            // 2. Push to WebSocket (Real-time Bell Update)
            try {
                messagingTemplate.convertAndSendToUser(
                        user.getUsername(),
                        "/queue/notifications",
                        notification
                );
                log.info("🔔 Real-time notification sent to {}", user.getUsername());
            } catch (Exception e) {
                log.error("Failed to send WebSocket notification to {}", user.getUsername(), e);
            }

            // 3. Send Email for HIGH/CRITICAL priority notifications
            if (request.getPriority().toString().equals("HIGH") ||
                    request.getPriority().toString().equals("CRITICAL")) {
                sendEmailNotification(user, request);
            }
        }
    }

    /**
     * Send email notification based on notification type
     */
    private void sendEmailNotification(AppUser user, NotificationRequest request) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("User {} has no email address. Skipping email notification.", user.getUsername());
            return;
        }

        try {
            switch (request.getType()) {
                case ASSET:
                    sendAssetExpiryEmail(user, request);
                    break;
                case TICKET:
                    sendTicketAssignmentEmail(user, request);
                    break;
                default:
                    // Send generic email for other types
                    sendGenericEmail(user, request);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Send asset expiry alert email
     */
    private void sendAssetExpiryEmail(AppUser user, NotificationRequest request) {
        if (request.getReferenceId() != null) {
            assetRepository.findById(request.getReferenceId()).ifPresent(asset -> {
                Map<String, Object> templateData = new HashMap<>();
                templateData.put("assetTag", asset.getAssetTag());
                templateData.put("assetName", asset.getName());
                templateData.put("serialNumber", asset.getSerialNumber());
                templateData.put("warrantyEndDate", asset.getWarrantyEndDate());
                templateData.put("amcEndDate", asset.getAmcEndDate());

                long daysRemaining = ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(),
                        asset.getWarrantyEndDate() != null ? asset.getWarrantyEndDate() : asset.getAmcEndDate()
                );
                templateData.put("daysRemaining", daysRemaining);
                templateData.put("assetUrl", "http://localhost:9090/itams/assets/" + asset.getId());

                emailService.sendHtmlEmail(
                        user.getEmail(),
                        "⚠️ Asset Expiry Alert: " + asset.getName(),
                        "emails/asset-expiry-alert",
                        templateData
                );
            });
        }
    }

    /**
     * Send ticket assignment email
     */
    private void sendTicketAssignmentEmail(AppUser user, NotificationRequest request) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("assigneeName", user.getFullName() != null ? user.getFullName() : user.getUsername());
        templateData.put("ticketTitle", request.getMessage());
        templateData.put("priority", "High"); // You can extract this from request if needed
        templateData.put("category", "General");
        templateData.put("assignedBy", "System");
        templateData.put("ticketUrl", "http://localhost:9090/tickets/" + request.getReferenceId());

        emailService.sendHtmlEmail(
                user.getEmail(),
                "🎟️ " + request.getTitle(),
                "emails/ticket-assignment",
                templateData
        );
    }

    /**
     * Send generic email for other notification types
     */
    private void sendGenericEmail(AppUser user, NotificationRequest request) {
        String subject = request.getTitle();
        String text = request.getMessage() + "\n\n" +
                "Module: " + request.getModule() + "\n" +
                "Priority: " + request.getPriority();

        emailService.sendTextEmail(user.getEmail(), subject, text);
    }

    private List<AppUser> findRecipients(NotificationRequest request) {
        if (request.getRecipientUserId() != null) {
            return appUserRepository.findById(request.getRecipientUserId())
                    .map(List::of).orElse(List.of());
        }
        else if (request.getRecipientRoleName() != null) {
            return appUserRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName().equalsIgnoreCase(request.getRecipientRoleName())))
                    .toList();
        }
        return List.of();
    }

    // --- Standard UI Methods ---

    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        if (notification.getUser().getId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}