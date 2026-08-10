package com.sujal.itsm.workflow.event;

import com.sujal.itsm.workflow.model.ApprovalRequest;
import org.springframework.context.ApplicationEvent;

public class WorkflowApprovedEvent extends ApplicationEvent {
    private final ApprovalRequest approvalRequest;

    public WorkflowApprovedEvent(Object source, ApprovalRequest approvalRequest) {
        super(source);
        this.approvalRequest = approvalRequest;
    }

    public ApprovalRequest getApprovalRequest() {
        return approvalRequest;
    }
}