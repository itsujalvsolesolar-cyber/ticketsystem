package com.sujal.itsm.core.offboarding.repository;

import com.sujal.itsm.core.offboarding.enums.OffboardingStatus;
import com.sujal.itsm.core.offboarding.model.OffboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingRequestRepository extends JpaRepository<OffboardingRequest, Long> {

    List<OffboardingRequest> findByStatus(OffboardingStatus status);

    List<OffboardingRequest> findByEmployeeId(Long employeeId);

    // Dashboard KPI: Count pending offboardings
    long countByStatus(OffboardingStatus status);
}