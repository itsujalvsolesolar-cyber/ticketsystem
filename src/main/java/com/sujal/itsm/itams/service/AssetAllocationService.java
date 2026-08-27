package com.sujal.itsm.itams.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sujal.itsm.core.notification.enums.NotificationPriority;
import com.sujal.itsm.core.notification.enums.NotificationType;
import com.sujal.itsm.core.notification.event.NotificationRequest;
import com.sujal.itsm.core.notification.service.NotificationService;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import com.sujal.itsm.itams.enums.AcceptanceStatus;
import com.sujal.itsm.itams.enums.AssetCondition;
import com.sujal.itsm.itams.enums.AssetHistoryAction;
import com.sujal.itsm.itams.enums.AssetStatus;
import com.sujal.itsm.itams.model.Asset;
import com.sujal.itsm.itams.model.AssetAllocation;
import com.sujal.itsm.itams.model.Employee;
import com.sujal.itsm.itams.repository.AssetAllocationRepository;
import com.sujal.itsm.itams.repository.AssetRepository;
import com.sujal.itsm.itams.repository.EmployeeRepository;
import com.sujal.itsm.workflow.enums.SignatureType;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssetAllocationService {

    private final AssetAllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AssetHistoryService historyService;
    private final NotificationService notificationService;

    public AssetAllocation findById(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Allocation not found"));
    }

    // ========== ALLOCATE (accessories + pending acceptance) ==========
    public AssetAllocation allocateAsset(Long assetId, Long employeeId, String condition,
                                         String notes, LocalDate expectedReturn, String accessories) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalArgumentException("Asset is not available for allocation. Current status: " + asset.getStatus());
        }

        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        AssetAllocation allocation = AssetAllocation.builder()
                .asset(asset)
                .employee(employee)
                .allocatedBy(currentUserService.getCurrentUser())
                .conditionAtIssue(AssetCondition.valueOf(condition))
                .notes(notes)
                .expectedReturnDate(expectedReturn)
                .accessories(accessories)
                .acceptanceStatus(AcceptanceStatus.PENDING)
                .isActive(true)
                .build();
        allocation = allocationRepository.save(allocation);

        historyService.record(asset, AssetHistoryAction.ALLOCATED,
                AssetStatus.AVAILABLE.name(), AssetStatus.ASSIGNED.name(),
                "Assigned to " + employee.getFullName()
                        + (accessories != null && !accessories.isBlank() ? " | Accessories: " + accessories : ""));

        notifyEmployee(employee, asset);
        return allocation;
    }

    // ========== EMPLOYEE DIGITAL ACCEPTANCE ==========
    public AssetAllocation acceptAllocation(Long allocationId, SignatureType signatureType,
                                            String signatureData, String ip, String userAgent) {
        AssetAllocation allocation = findById(allocationId);

        if (allocation.getAcceptanceStatus() == AcceptanceStatus.ACCEPTED) {
            throw new IllegalArgumentException("This asset has already been accepted.");
        }

        AppUser currentUser = currentUserService.getCurrentUser();
        allocation.setAcceptanceStatus(AcceptanceStatus.ACCEPTED);
        allocation.setAcceptedAt(LocalDateTime.now());
        allocation.setAcceptedBy(currentUser);
        allocation.setSignatureType(signatureType);
        allocation.setSignatureData(signatureData);
        allocation.setAcceptanceIp(ip);
        allocation.setAcceptanceUserAgent(userAgent);
        allocation = allocationRepository.save(allocation);

        historyService.record(allocation.getAsset(), AssetHistoryAction.ACCEPTED,
                AcceptanceStatus.PENDING.name(), AcceptanceStatus.ACCEPTED.name(),
                "Employee digital acceptance by " + currentUser.getUsername());

        if (allocation.getAllocatedBy() != null) {
            NotificationRequest req = NotificationRequest.builder()
                    .recipientUserId(allocation.getAllocatedBy().getId())
                    .title("✅ Asset accepted")
                    .message(allocation.getEmployee().getFullName() + " accepted "
                            + allocation.getAsset().getAssetTag() + " with digital signature.")
                    .priority(NotificationPriority.LOW)
                    .type(NotificationType.ASSET)
                    .module("ITAMS")
                    .referenceId(allocation.getAsset().getId())
                    .build();
            notificationService.createAndDispatch(req);
        }
        return allocation;
    }

    // ========== RETURN (with history) ==========
    public void returnAsset(Long allocationId, String returnCondition, String notes) {
        AssetAllocation allocation = findById(allocationId);
        if (!allocation.getIsActive()) {
            throw new IllegalArgumentException("This allocation is already closed/returned.");
        }

        allocation.setIsActive(false);
        allocation.setReturnDate(LocalDate.now());
        allocation.setConditionAtReturn(AssetCondition.valueOf(returnCondition));
        String existing = allocation.getNotes() != null ? allocation.getNotes() : "";
        allocation.setNotes(existing + " | Return: " + (notes != null ? notes : "No notes"));
        allocationRepository.save(allocation);

        Asset asset = allocation.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        historyService.record(asset, AssetHistoryAction.RETURNED,
                AssetStatus.ASSIGNED.name(), AssetStatus.AVAILABLE.name(),
                "Returned in condition " + returnCondition + (notes != null ? " | " + notes : ""));
    }

    public AssetAllocation getCurrentAllocation(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        List<AssetAllocation> allocations = allocationRepository.findActiveAllocationsByAsset(asset);
        return allocations.isEmpty() ? null : allocations.get(0);
    }

    // ========== HELPERS ==========
    private void notifyEmployee(Employee employee, Asset asset) {
        if (employee.getEmployeeId() == null) return;
        userRepository.findByEmployeeId(employee.getEmployeeId()).ifPresent(appUser -> {
            NotificationRequest req = NotificationRequest.builder()
                    .recipientUserId(appUser.getId())
                    .title(" Asset assigned – acceptance required")
                    .message(asset.getName() + " (" + asset.getAssetTag()
                            + ") has been assigned to you. Please review and accept.")
                    .priority(NotificationPriority.MEDIUM)
                    .type(NotificationType.ASSET)
                    .module("ITAMS")
                    .referenceId(asset.getId())
                    .build();
            notificationService.createAndDispatch(req);
        });
    }
}