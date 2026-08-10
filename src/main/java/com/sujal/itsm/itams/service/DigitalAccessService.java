package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.itams.model.DigitalAccess;
import com.sujal.itsm.itams.repository.DigitalAccessRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DigitalAccessService {

    private final DigitalAccessRepository accessRepository;
    private final CurrentUserService currentUserService;

    public List<DigitalAccess> findAllActive() {
        return accessRepository.findAllActive();
    }

    public DigitalAccess create(DigitalAccess access) {
        access.setAssignedBy(currentUserService.getCurrentUser());
        return accessRepository.save(access);
    }

    public void revoke(Long id) {
        DigitalAccess access = accessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Access record not found"));
        access.setIsActive(false);
        accessRepository.save(access);
    }
}