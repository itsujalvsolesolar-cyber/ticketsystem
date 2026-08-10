package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.model.SoftwareLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {

    @Query("SELECT s FROM SoftwareLicense s " +
           "LEFT JOIN FETCH s.employee " +
           "LEFT JOIN FETCH s.softwareCatalog " + // ✅ Added
           "LEFT JOIN FETCH s.assignedBy " +
           "WHERE s.isActive = true ORDER BY s.assignedDate DESC")
    List<SoftwareLicense> findAllActive();

    @Query("SELECT s FROM SoftwareLicense s " +
           "LEFT JOIN FETCH s.employee " +
           "LEFT JOIN FETCH s.softwareCatalog " + // ✅ Added
           "LEFT JOIN FETCH s.assignedBy " +
           "WHERE s.employee = :employee AND s.isActive = true")
    List<SoftwareLicense> findByEmployee(@Param("employee") Employee employee);
}