package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.SoftwareCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SoftwareCatalogRepository extends JpaRepository<SoftwareCatalog, Long> {
    List<SoftwareCatalog> findByIsActiveTrueOrderByNameAsc();
}