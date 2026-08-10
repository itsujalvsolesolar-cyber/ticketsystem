package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.NasPermissionLevel;
import com.sujal.itsm.itams.enums.NasRequestStatus;
import com.sujal.itsm.itams.model.NasAccessRequest;
import com.sujal.itsm.itams.model.NasFolder;
import com.sujal.itsm.itams.repository.NasAccessRequestRepository;
import com.sujal.itsm.itams.repository.NasFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.event.WorkflowApprovedEvent;
import com.sujal.itsm.workflow.event.WorkflowRejectedEvent;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import com.sujal.itsm.workflow.service.WorkflowEngineService;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NasService {

    private final NasFolderRepository nasFolderRepository;
    private final NasAccessRequestRepository nasAccessRequestRepository;
    private final AppUserRepository appUserRepository;
    private final WorkflowEngineService workflowEngineService;

    public NasFolder createFolder(String folderName, String networkPath, String description, String sensitivityLevel, boolean requiresExecutiveApproval) {
        NasFolder folder = NasFolder.builder()
                .folderName(folderName.toUpperCase())
                .networkPath(networkPath)
                .description(description)
                .sensitivityLevel(sensitivityLevel)
                .requiresExecutiveApproval(requiresExecutiveApproval)
                .build();
        return nasFolderRepository.save(folder);
    }

    public List<NasFolder> getAllFolders() {
        return nasFolderRepository.findAll();
    }

    public NasAccessRequest requestAccess(Long folderId, Long employeeId, Long requestedById, NasPermissionLevel permissionLevel, String remarks) {
        return requestAccessForEmployee(folderId, employeeId, requestedById, permissionLevel, false, null, remarks);
    }

    public NasAccessRequest requestAccessForEmployee(Long folderId, Long employeeId, Long requestedById, NasPermissionLevel permissionLevel, boolean isTemporary, LocalDateTime temporaryEndDate, String remarks) {
        NasFolder folder = nasFolderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        AppUser employee = appUserRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        AppUser requestedBy = appUserRepository.findById(requestedById)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        boolean alreadyApproved = nasAccessRequestRepository.findByEmployeeAndStatus(employee, NasRequestStatus.APPROVED)
                .stream().anyMatch(req -> req.getFolder().getId().equals(folderId));
        if (alreadyApproved) {
            throw new RuntimeException("Employee already has approved access to this folder.");
        }

        NasRequestStatus initialStatus = folder.getRequiresExecutiveApproval() ? NasRequestStatus.PENDING_MD : NasRequestStatus.PENDING_IT;

        NasAccessRequest request = NasAccessRequest.builder()
                .folder(folder)
                .employee(employee)
                .requestedBy(requestedBy)
                .permissionLevel(permissionLevel)
                .status(initialStatus)
                .remarks(remarks)
                .notifyOnRevocation(true)
                .isTemporary(isTemporary)
                .temporaryEndDate(temporaryEndDate)
                .build();

        NasAccessRequest savedRequest = nasAccessRequestRepository.save(request);

        // ✅ START THE APPROVAL WORKFLOW
        workflowEngineService.startWorkflow(savedRequest.getId(), WorkflowModuleType.NAS_ACCESS, requestedBy);

        return savedRequest;
    }

    public List<NasAccessRequest> getPendingItRequests() {
        return nasAccessRequestRepository.findByStatus(NasRequestStatus.PENDING_IT);
    }

    public List<NasAccessRequest> getPendingMdRequests() {
        return nasAccessRequestRepository.findByStatus(NasRequestStatus.PENDING_MD);
    }

    public List<NasAccessRequest> getApprovedRequestsForEmployee(Long employeeId) {
        AppUser employee = appUserRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return nasAccessRequestRepository.findByEmployeeAndStatus(employee, NasRequestStatus.APPROVED);
    }

    public List<NasAccessRequest> getAllRequests() {
        return nasAccessRequestRepository.findAll();
    }

    public NasAccessRequest approveByIt(Long requestId, Long approverId, String remarks) {
        NasAccessRequest request = nasAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        AppUser approver = appUserRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (request.getFolder().getRequiresExecutiveApproval()) {
            request.setStatus(NasRequestStatus.PENDING_MD);
        } else {
            request.setStatus(NasRequestStatus.APPROVED);
        }
        request.setApprovedByIt(approver);
        request.setRemarks(remarks != null ? remarks : request.getRemarks());

        return nasAccessRequestRepository.save(request);
    }

    public NasAccessRequest approveByMd(Long requestId, Long approverId, String remarks) {
        NasAccessRequest request = nasAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        AppUser approver = appUserRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        request.setStatus(NasRequestStatus.APPROVED);
        request.setApprovedByMd(approver);
        request.setRemarks(remarks != null ? remarks : request.getRemarks());

        return nasAccessRequestRepository.save(request);
    }

    public NasAccessRequest rejectRequest(Long requestId, Long approverId, String remarks) {
        NasAccessRequest request = nasAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        AppUser approver = appUserRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        request.setStatus(NasRequestStatus.REJECTED);
        request.setRemarks(remarks != null ? remarks : request.getRemarks());

        return nasAccessRequestRepository.save(request);
    }

    public NasAccessRequest revokeAccess(Long requestId, Long revokerId, boolean notify) {
        NasAccessRequest request = nasAccessRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(NasRequestStatus.REVOKED);
        request.setNotifyOnRevocation(notify);

        return nasAccessRequestRepository.save(request);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWorkflowApproved(WorkflowApprovedEvent event) {
        ApprovalRequest approval = event.getApprovalRequest();
        if (approval.getModuleType() == WorkflowModuleType.NAS_ACCESS) {
            NasAccessRequest nasRequest = nasAccessRequestRepository.findById(approval.getReferenceId())
                    .orElseThrow(() -> new RuntimeException("NAS Request not found"));

            nasRequest.setStatus(NasRequestStatus.APPROVED);
            nasAccessRequestRepository.save(nasRequest);

            // TODO: Future - Trigger actual Active Directory / NAS provisioning script here
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWorkflowRejected(WorkflowRejectedEvent event) {
        ApprovalRequest approval = event.getApprovalRequest();
        if (approval.getModuleType() == WorkflowModuleType.NAS_ACCESS) {
            NasAccessRequest nasRequest = nasAccessRequestRepository.findById(approval.getReferenceId())
                    .orElseThrow(() -> new RuntimeException("NAS Request not found"));

            nasRequest.setStatus(NasRequestStatus.REJECTED);

            // Append rejection remarks to the NAS request
            String lastRemark = approval.getActions().stream()
                    .filter(a -> a.getAction() == com.sujal.itsm.workflow.enums.ApprovalActionType.REJECTED)
                    .findFirst().map(com.sujal.itsm.workflow.model.ApprovalAction::getRemarks).orElse("No reason provided");

            nasRequest.setRemarks((nasRequest.getRemarks() != null ? nasRequest.getRemarks() + " | " : "") + "Rejected: " + lastRemark);
            nasAccessRequestRepository.save(nasRequest);
        }
    }
}