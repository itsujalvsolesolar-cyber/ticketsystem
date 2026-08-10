package com.sujal.itsm.workflow.repository;

import com.sujal.itsm.workflow.enums.ApprovalStatus;
import com.sujal.itsm.workflow.enums.WorkflowModuleType;
import com.sujal.itsm.workflow.model.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    Optional<ApprovalRequest> findByReferenceIdAndModuleType(Long referenceId, WorkflowModuleType moduleType);

    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    List<ApprovalRequest> findByRequesterIdAndStatus(Long requesterId, ApprovalStatus status);

    long countByStatus(com.sujal.itsm.workflow.enums.ApprovalStatus status);
}