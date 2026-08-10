package com.sujal.itsm.itams.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.itams.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

  Optional<Supplier> findByName(String name);

  @Query("SELECT s FROM Supplier s WHERE s.isActive = true ORDER BY s.name")
  List<Supplier> findAllActive();

  @Query("SELECT s FROM Supplier s ORDER BY s.name")
  List<Supplier> findAllOrderByName();

  boolean existsByName(String name);

  boolean existsByGstNumber(String gstNumber);

  long countByIsActive(Boolean isActive);
}
