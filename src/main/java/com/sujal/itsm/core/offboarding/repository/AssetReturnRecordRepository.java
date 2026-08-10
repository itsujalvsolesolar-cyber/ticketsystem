package com.sujal.itsm.core.offboarding.repository;

import com.sujal.itsm.core.offboarding.enums.AssetReturnStatus;
import com.sujal.itsm.core.offboarding.model.AssetReturnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetReturnRecordRepository extends JpaRepository<AssetReturnRecord, Long> {

    List<AssetReturnRecord> findByRequestId(Long requestId);

    List<AssetReturnRecord> findByStatus(AssetReturnStatus status);

    // Dashboard KPI: Count unreturned/lost assets
    long countByStatus(AssetReturnStatus status);
}