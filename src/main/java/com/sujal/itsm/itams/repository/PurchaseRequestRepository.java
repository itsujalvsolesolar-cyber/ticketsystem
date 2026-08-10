package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    // Find all requests with a specific status (e.g., PENDING_APPROVAL)
    List<PurchaseRequest> findByStatus(PurchaseRequest.PRStatus status);

    // Find all requests made by a specific user
    List<PurchaseRequest> findByRequesterId(Long requesterId);

    long countByStatus(com.sujal.itsm.itams.model.PurchaseRequest.PRStatus status);
}