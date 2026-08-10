package com.sujal.itsm.core.notification.service;

import com.sujal.itsm.core.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    @Async // Process in background so it doesn't slow down the main thread
    public void handleNotificationEvent(NotificationEvent event) {
        log.info(" Received Notification Event: {}", event.getRequest().getTitle());
        try {
            notificationService.createAndDispatch(event.getRequest());
        } catch (Exception e) {
            log.error("❌ Failed to process notification event", e);
        }
    }
}