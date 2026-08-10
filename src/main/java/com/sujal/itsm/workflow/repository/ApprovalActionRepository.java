package com.sujal.itsm.workflow.repository;

import com.sujal.itsm.workflow.model.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {

    // ✅ THIS MUST BE 'SignedAt', NOT 'Timestamp'
    List<ApprovalAction> findByApprovalRequestIdOrderBySignedAtAsc(Long approvalRequestId);
}