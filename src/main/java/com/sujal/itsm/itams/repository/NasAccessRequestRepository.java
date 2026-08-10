package com.sujal.itsm.itams.repository;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.itams.enums.NasRequestStatus;
import com.sujal.itsm.itams.model.NasAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NasAccessRequestRepository extends JpaRepository<NasAccessRequest, Long> {
    List<NasAccessRequest> findByEmployeeAndStatus(AppUser employee, NasRequestStatus status);
    List<NasAccessRequest> findByStatus(NasRequestStatus status);
    List<NasAccessRequest> findByFolderId(Long folderId);

    // ✅ ADDED: Required for the executive dashboard history
    List<NasAccessRequest> findByEmployee(AppUser employee);
}