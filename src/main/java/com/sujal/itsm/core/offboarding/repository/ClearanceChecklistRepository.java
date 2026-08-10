package com.sujal.itsm.core.offboarding.repository;

import com.sujal.itsm.core.offboarding.enums.ClearanceDepartment;
import com.sujal.itsm.core.offboarding.enums.ClearanceStatus;
import com.sujal.itsm.core.offboarding.model.ClearanceChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClearanceChecklistRepository extends JpaRepository<ClearanceChecklist, Long> {

    List<ClearanceChecklist> findByRequestId(Long requestId);

    Optional<ClearanceChecklist> findByRequestIdAndDepartment(Long requestId, ClearanceDepartment department);

    // Dashboard KPI: Count pending IT clearances
    long countByDepartmentAndStatus(ClearanceDepartment department, ClearanceStatus status);
}