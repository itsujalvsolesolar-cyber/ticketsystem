package com.sujal.itsm.itams.repository;

import com.sujal.itsm.itams.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.active = true ORDER BY e.firstName ASC, e.lastName ASC")
    List<Employee> findByIsActiveTrueOrderByFullNameAsc();

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<Employee> findByUserUsername(@Param("username") String username);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.user LEFT JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);
}