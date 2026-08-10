package com.sujal.itsm.itams.service;

import com.sujal.itsm.itams.model.SoftwareCatalog;
import com.sujal.itsm.itams.repository.SoftwareCatalogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SoftwareCatalogService {

    private final SoftwareCatalogRepository catalogRepository;

    public List<SoftwareCatalog> findAllActive() {
        return catalogRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public SoftwareCatalog findById(Long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Software not found"));
    }

    public SoftwareCatalog create(SoftwareCatalog catalog) {
        return catalogRepository.save(catalog);
    }

    public SoftwareCatalog update(Long id, SoftwareCatalog details) {
        SoftwareCatalog catalog = findById(id);
        catalog.setName(details.getName());
        catalog.setVendor(details.getVendor());
        catalog.setTotalSeats(details.getTotalSeats());
        catalog.setCostPerSeat(details.getCostPerSeat());
        catalog.setDescription(details.getDescription());
        catalog.setIsActive(details.getIsActive());
        return catalogRepository.save(catalog);
    }

    public void delete(Long id) {
        SoftwareCatalog catalog = findById(id);
        catalog.setIsActive(false);
        catalogRepository.save(catalog);
    }
}