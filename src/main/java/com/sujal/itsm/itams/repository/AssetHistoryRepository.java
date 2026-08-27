package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {
    List<AssetHistory> findByAssetIdOrderByCreatedAtDesc(Long assetId);
}