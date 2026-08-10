package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.model.SoftwareLicense;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import com.sujal.itsm.itams.repository.SoftwareLicenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SoftwareLicenseService {

    private final SoftwareLicenseRepository licenseRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public List<SoftwareLicense> findAllActive() {
        return licenseRepository.findAllActive();
    }

    public SoftwareLicense create(SoftwareLicense license) {
        license.setAssignedBy(currentUserService.getCurrentUser());
        return licenseRepository.save(license);
    }

    public void revoke(Long id) {
        SoftwareLicense license = licenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("License not found"));
        license.setIsActive(false);
        licenseRepository.save(license);
    }
}