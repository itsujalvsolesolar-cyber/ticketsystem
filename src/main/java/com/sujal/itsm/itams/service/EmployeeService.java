package com.sujal.itsm.itams.service;

import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> findAllActive() {
        return employeeRepository.findByIsActiveTrueOrderByFullNameAsc();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    public Employee create(Employee employee) {
        // ✅ FIX: Convert empty strings to null for unique columns
        if (employee.getEmployeeId() != null && employee.getEmployeeId().trim().isEmpty()) {
            employee.setEmployeeId(null);
        }
        if (employee.getEmail() != null && employee.getEmail().trim().isEmpty()) {
            employee.setEmail(null);
        }
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, Employee details) {
        Employee employee = findById(id);
        employee.setFullName(details.getFullName());

        // ✅ FIX: Convert empty strings to null for unique columns
        employee.setEmployeeId(details.getEmployeeId() != null && details.getEmployeeId().trim().isEmpty()
                ? null : details.getEmployeeId());
        employee.setEmail(details.getEmail() != null && details.getEmail().trim().isEmpty()
                ? null : details.getEmail());

        employee.setPhone(details.getPhone());
        employee.setDepartment(details.getDepartment());
        employee.setDesignation(details.getDesignation());
        employee.setIsActive(details.getIsActive());

        return employeeRepository.save(employee);
    }

    public void delete(Long id) {
        Employee employee = findById(id);
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }
}