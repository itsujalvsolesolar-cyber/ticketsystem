package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.DigitalAccess;
import com.sujal.itsm.itams.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalAccessRepository extends JpaRepository<DigitalAccess, Long> {

    @Query("SELECT d FROM DigitalAccess d " +
           "LEFT JOIN FETCH d.employee " +
           "LEFT JOIN FETCH d.assignedBy " +
           "WHERE d.isActive = true ORDER BY d.assignedDate DESC")
    List<DigitalAccess> findAllActive();

    @Query("SELECT d FROM DigitalAccess d " +
           "LEFT JOIN FETCH d.employee " +
           "LEFT JOIN FETCH d.assignedBy " +
           "WHERE d.employee = :employee AND d.isActive = true")
    List<DigitalAccess> findByEmployee(@Param("employee") Employee employee);

    long countByEmployee_Id(Long employeeId);

    List<DigitalAccess> findByEmployee_Id(Long employeeId);
}