package com.sujal.itsm.itams.service;

import com.sujal.itsm.itams.model.Warehouse;
import com.sujal.itsm.itams.repository.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> findAllActive() {
        return warehouseRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
    }

    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public Warehouse update(Long id, Warehouse details) {
        Warehouse warehouse = findById(id);
        warehouse.setName(details.getName());
        warehouse.setLocation(details.getLocation());
        warehouse.setManagerName(details.getManagerName());
        warehouse.setCapacity(details.getCapacity());
        warehouse.setIsActive(details.getIsActive());
        return warehouseRepository.save(warehouse);
    }

    public void delete(Long id) {
        Warehouse warehouse = findById(id);
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
    }
}