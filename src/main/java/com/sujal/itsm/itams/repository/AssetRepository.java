package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

  Optional<Asset> findByAssetTag(String assetTag);

  Optional<Asset> findBySerialNumber(String serialNumber);

  boolean existsByAssetTag(String assetTag);

  boolean existsBySerialNumber(String serialNumber);

  @Query("SELECT a FROM Asset a WHERE a.category = :category ORDER BY a.id DESC LIMIT 1")
  Optional<Asset> findLatestByCategory(@Param("category") AssetCategory category);

  @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status")
  long countByStatus(@Param("status") AssetStatus status);

  @Query("SELECT COUNT(a) FROM Asset a WHERE a.category = :category")
  long countByCategory(@Param("category") AssetCategory category);

  @Query("SELECT a FROM Asset a WHERE a.warrantyEndDate <= :date AND a.isActive = true")
  List<Asset> findAssetsWithExpiringWarranty(@Param("date") LocalDate date);

  @Query("SELECT a FROM Asset a WHERE a.amcEndDate <= :date AND a.isActive = true")
  List<Asset> findAssetsWithExpiringAmc(@Param("date") LocalDate date);

  // ✅ FIXED: Removed "assignedTo" since it doesn't exist in Asset.java
  @EntityGraph(attributePaths = {"category", "brand", "supplier"})
  @Query("SELECT a FROM Asset a WHERE a.isActive = true ORDER BY a.createdAt DESC")
  Page<Asset> findAllActive(Pageable pageable);

  // ✅ FIXED: Removed "assignedTo"
  @Override
  @EntityGraph(attributePaths = {"category", "brand", "supplier"})
  Optional<Asset> findById(Long id);

  @Query("SELECT a FROM Asset a WHERE a.status = :status AND a.isActive = true")
  List<Asset> findAllByStatus(@Param("status") AssetStatus status);

  @Query("""
        SELECT COUNT(a) FROM Asset a 
        WHERE a.warrantyEndDate BETWEEN :startDate AND :endDate
    """)
  long countWarrantyExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  // ✅ FIXED: Removed "assignedTo" to prevent the crash
  @EntityGraph(attributePaths = {"category", "brand", "supplier"})
  @Query("SELECT a FROM Asset a WHERE a.isActive = true AND " +
          "(a.warrantyEndDate BETWEEN :today AND :futureDate OR " +
          "a.amcEndDate BETWEEN :today AND :futureDate)")
  List<Asset> findAssetsExpiringWithin(@Param("today") LocalDate today, @Param("futureDate") LocalDate futureDate);

  @Query("SELECT a.category.name, COUNT(a) FROM Asset a WHERE a.isActive = true GROUP BY a.category.name")
  List<Object[]> countAssetsByCategory();

  // ✅ CHANGE THIS to match your actual String field names
  Optional<Asset> findByAssetTagOrSerialNumber(String assetTag, String serialNumber);


}