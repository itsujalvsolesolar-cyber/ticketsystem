package com.sujal.itsm.itams.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sujal.itsm.itams.model.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

  Optional<Brand> findByName(String name);

  @Query("SELECT b FROM Brand b WHERE b.isActive = true ORDER BY b.name")
  List<Brand> findAllActive();

  @Query("SELECT b FROM Brand b ORDER BY b.name")
  List<Brand> findAllOrderByname();

  boolean existsByName(String name);

  long countByIsActive(Boolean isActive);
}
