package com.sujal.itsm.core.security;

import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("securityEvaluator")
@RequiredArgsConstructor
public class CustomSecurityEvaluator {

    private final EmployeeRepository employeeRepository;
    private final AssetAllocationRepository allocationRepository;

    @Transactional(readOnly = true)
    public boolean isEmployeeOwner(Authentication authentication, Long employeeId) {
        if (authentication == null || !authentication.isAuthenticated() || employeeId == null) {
            return false;
        }
        if (hasAnyRole(authentication, "ADMIN", "STAFF")) {
            return true;
        }

        String principalUsername = authentication.getName();
        return employeeRepository.findById(employeeId)
            .map(emp -> emp.getUser() != null && principalUsername.equalsIgnoreCase(emp.getUser().getUsername()))
            .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAllocationOwner(Authentication authentication, Long allocationId) {
        if (authentication == null || !authentication.isAuthenticated() || allocationId == null) {
            return false;
        }
        if (hasAnyRole(authentication, "ADMIN", "STAFF")) {
            return true;
        }

        String principalUsername = authentication.getName();
        return allocationRepository.findById(allocationId)
            .map(AssetAllocation::getEmployee)
            .map(emp -> emp.getUser() != null && principalUsername.equalsIgnoreCase(emp.getUser().getUsername()))
            .orElse(false);
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        return authentication.getAuthorities().stream().anyMatch(granted -> {
            for (String role : roles) {
                if (granted.getAuthority().equals("ROLE_" + role)) {
                    return true;
                }
            }
            return false;
        });
    }
}