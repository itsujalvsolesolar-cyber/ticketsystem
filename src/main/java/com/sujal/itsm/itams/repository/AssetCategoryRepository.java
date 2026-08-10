package com.sujal.itsm.itams.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.itams.model.AssetCategory;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {

  Optional<AssetCategory> findByName(String name);

  Optional<AssetCategory> findByPrefix(String prefix);

  @Query("SELECT c FROM AssetCategory c WHERE c.isActive = true ORDER BY c.name")
  List<AssetCategory> findAllActive();

  @Query("SELECT c FROM AssetCategory c ORDER BY c.name")
  List<AssetCategory> findAllOrderByname();

  boolean existsByName(String name);

  boolean existsByPrefix(String prefix);

  long countByIsActive(Boolean isActive);
}
