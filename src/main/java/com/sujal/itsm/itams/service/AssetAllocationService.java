package com.sujal.itsm.itams.service;

import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.AssetRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetAllocationService {

    private final AssetAllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final CurrentUserService currentUserService;
    private final AppUserRepository userRepository; // ✅ Injected UserRepository
    private final EmployeeRepository employeeRepository;

    /**
     * Allocates an asset to an employee.
     */
    public AssetAllocation allocateAsset(Long assetId, Long employeeId, String condition, String notes, LocalDate expectedReturn) {
        // 1. Fetch Asset
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        // 2. Fetch Employee (AppUser)
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        // 3. Check if asset is available
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalArgumentException("Asset is not available for allocation. Current status: " + asset.getStatus());
        }

        // 4. Update Asset Status
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        // 5. Create Allocation Record
        AssetAllocation allocation = AssetAllocation.builder()
                .asset(asset)
                .employee(employee)
                .allocatedBy(currentUserService.getCurrentUser())
                .conditionAtIssue(com.sujal.itsm.itams.enums.AssetCondition.valueOf(condition))
                .notes(notes)
                .expectedReturnDate(expectedReturn)
                .isActive(true)
                .build();

        return allocationRepository.save(allocation);
    }

    /**
     * Returns an asset from an employee.
     */
    public void returnAsset(Long allocationId, String returnCondition, String notes) {
        AssetAllocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new EntityNotFoundException("Allocation not found"));

        if (!allocation.getIsActive()) {
            throw new IllegalArgumentException("This allocation is already closed/returned.");
        }

        // 1. Update Allocation Record
        allocation.setIsActive(false); // Fixed: method is setIsActive, not setActive
        allocation.setReturnDate(LocalDate.now());
        allocation.setConditionAtReturn(com.sujal.itsm.itams.enums.AssetCondition.valueOf(returnCondition));

        // Append return notes if there are existing notes
        String existingNotes = allocation.getNotes() != null ? allocation.getNotes() : "";
        allocation.setNotes(existingNotes + " | Return: " + (notes != null ? notes : "No notes"));

        allocationRepository.save(allocation);

        // 2. Update Asset Status back to AVAILABLE
        Asset asset = allocation.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);
    }

    // Helper to get current active allocation for an asset
    public AssetAllocation getCurrentAllocation(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        // Fetch as a list to avoid crashes if there are duplicate active records
        List<AssetAllocation> allocations = allocationRepository.findActiveAllocationsByAsset(asset);

        // Return the most recent active allocation, or null if none exist
        return allocations.isEmpty() ? null : allocations.get(0);
    }
}