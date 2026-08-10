package com.sujal.itsm.core.notification.event;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent {
    private final NotificationRequest request;

    public NotificationEvent(Object source, NotificationRequest request) {
        super(source);
        this.request = request;
    }

    public NotificationRequest getRequest() {
        return request;
    }
}