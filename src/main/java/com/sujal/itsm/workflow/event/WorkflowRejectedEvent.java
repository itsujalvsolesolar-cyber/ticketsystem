package com.sujal.itsm.workflow.event;

import com.sujal.itsm.workflow.model.ApprovalRequest;
import org.springframework.context.ApplicationEvent;

public class WorkflowRejectedEvent extends ApplicationEvent {
    private final ApprovalRequest approvalRequest;

    public WorkflowRejectedEvent(Object source, ApprovalRequest approvalRequest) {
        super(source);
        this.approvalRequest = approvalRequest;
    }

    public ApprovalRequest getApprovalRequest() {
        return approvalRequest;
    }
}