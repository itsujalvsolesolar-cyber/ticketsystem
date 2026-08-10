package com.sujal.itsm.core.offboarding.event;

import com.sujal.itsm.core.offboarding.model.OffboardingRequest;
import org.springframework.context.ApplicationEvent;

public class OffboardingCompletedEvent extends ApplicationEvent {
    private final OffboardingRequest request;

    public OffboardingCompletedEvent(Object source, OffboardingRequest request) {
        super(source);
        this.request = request;
    }

    public OffboardingRequest getRequest() {
        return request;
    }
}