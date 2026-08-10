package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAllocationRepository extends JpaRepository<AssetAllocation, Long> {

    // ✅ Changed to List to prevent NonUniqueResultException if multiple exist
    @Query("SELECT a FROM AssetAllocation a JOIN FETCH a.employee JOIN FETCH a.asset WHERE a.asset = :asset AND a.isActive = true ORDER BY a.allocationDate DESC")
    List<AssetAllocation> findActiveAllocationsByAsset(@Param("asset") Asset asset);

    @Query("SELECT a FROM AssetAllocation a JOIN FETCH a.employee JOIN FETCH a.asset WHERE a.employee = :employee AND a.isActive = true")
    List<AssetAllocation> findByEmployeeAndIsActiveTrue(@Param("employee") Employee employee);

    @Query("SELECT a FROM AssetAllocation a JOIN FETCH a.employee JOIN FETCH a.asset WHERE a.asset = :asset ORDER BY a.allocationDate DESC")
    List<AssetAllocation> findHistoryByAsset(@Param("asset") Asset asset);
}