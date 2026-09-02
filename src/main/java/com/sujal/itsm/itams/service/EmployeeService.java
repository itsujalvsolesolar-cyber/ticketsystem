package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.model.Department;
import com.sujal.itsm.core.user.model.Role;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.core.user.repository.DepartmentRepository;
import com.sujal.itsm.core.user.repository.RoleRepository;
import com.sujal.itsm.itams.dto.EmployeeOnboardingDTO;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Employee> findAllActive() {
        return employeeRepository.findByIsActiveTrueOrderByFullNameAsc();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findByIdWithDetails(id);
    }

    /**
     * PHASE 16.1: Unified Joiner Workflow
     * Creates the Employee, provisions the AppUser identity, and assigns ROLE_EMPLOYEE atomically.
     */
    @Transactional
    public Employee onboardEmployee(EmployeeOnboardingDTO dto) {
        // 1. Validate uniqueness
        if (appUserRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email '" + dto.getEmail() + "' is already registered.");
        }

        // 2. Fetch Dependencies
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("System role 'ROLE_EMPLOYEE' is missing from the database."));

        // 3. Provision AppUser (System Identity)
        AppUser user = AppUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getTemporaryPassword()))
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .fullName(dto.getFirstName() + " " + dto.getLastName())
                .isActive(true)
                .isEmailVerified(false) // Require email verification or first-login change
                .accountNonLocked(true)
                .department(department)
                .roles(new HashSet<>(Collections.singletonList(employeeRole)))
                .build();

        AppUser savedUser = appUserRepository.save(user);
        log.info("✅ Provisioned AppUser identity: {} ({})", savedUser.getUsername(), savedUser.getEmail());

        // 4. Create Employee (Business Profile) & Link
        Employee employee = Employee.builder()
                .employeeCode("EMP-" + String.format("%05d", savedUser.getId()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .department(department)
                .dateOfJoining(dto.getDateOfJoining() != null ? dto.getDateOfJoining() : LocalDate.now())
                .active(true)
                .user(savedUser) // Establishes the @OneToOne link
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("✅ Created Employee profile: {} ({})", savedEmployee.getEmployeeCode(), savedEmployee.getFullName());

        // 5. Trigger Welcome Email (Async/Event driven in production)
        if (dto.isSendActivationEmail()) {
            log.info("📧 Dispatching onboarding credentials to {}", dto.getEmail());
            // emailService.sendWelcomeEmail(dto.getEmail(), dto.getUsername(), dto.getTemporaryPassword());
        }

        return savedEmployee;
    }

    @Transactional
    public void update(Long id, Employee employeeDetails) {
        Employee existing = findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        existing.setFirstName(employeeDetails.getFirstName());
        existing.setLastName(employeeDetails.getLastName());
        existing.setPhone(employeeDetails.getPhone());
        existing.setJobTitle(employeeDetails.getJobTitle());
        existing.setDepartment(employeeDetails.getDepartment());
        
        // Sync AppUser if names/emails change
        if (existing.getUser() != null) {
            existing.getUser().setFirstName(employeeDetails.getFirstName());
            existing.getUser().setLastName(employeeDetails.getLastName());
            existing.getUser().setFullName(employeeDetails.getFirstName() + " " + employeeDetails.getLastName());
            appUserRepository.save(existing.getUser());
        }
        employeeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Employee existing = findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        existing.setActive(false);
        if (existing.getUser() != null) {
            existing.getUser().setActive(false); // Disables login (Leaver workflow)
            appUserRepository.save(existing.getUser());
        }
        employeeRepository.save(existing);
    }
}