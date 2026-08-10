package com.sujal.itsm.core.notification.event;

import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationRequest {
    private Long recipientUserId;      // Specific user
    private String recipientRoleName;  // Or everyone with this role (e.g., "IT_MANAGER")

    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationType type;
    private String module;
    private Long referenceId;
}