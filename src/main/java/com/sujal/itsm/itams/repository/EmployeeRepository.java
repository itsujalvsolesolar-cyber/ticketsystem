package com.sujal.itsm.itams.repository;

import java.util.Optional;

import com.sujal.itsm.itams.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByIsActiveTrueOrderByFullNameAsc();

    Optional<Employee> findByEmployeeId(String employeeId);
}