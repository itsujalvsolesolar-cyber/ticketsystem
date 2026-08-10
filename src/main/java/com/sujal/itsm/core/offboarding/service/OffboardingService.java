package com.sujal.itsm.core.offboarding.service;

import com.sujal.itsm.core.offboarding.dto.ClearanceSubmissionRequest;
import com.sujal.itsm.core.offboarding.dto.OffboardingInitiationRequest;
import com.sujal.itsm.core.offboarding.enums.*;
import com.sujal.itsm.core.offboarding.model.*;
import com.sujal.itsm.core.offboarding.repository.*;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import com.sujal.itsm.core.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffboardingService {

    private final OffboardingRequestRepository offboardingRepo;
    private final ClearanceChecklistRepository clearanceRepo;
    private final AssetReturnRecordRepository assetReturnRepo;
    private final AppUserRepository userRepo;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher; // Add this

    // TODO: Inject your actual AssetAllocationRepository here if you have one!
    // private final AssetAllocationRepository assetAllocationRepo;

    /**
     * 1. INITIATE OFFBOARDING
     * Creates the request and assigns departmental checklists.
     */
    @Transactional
    public OffboardingRequest initiateOffboarding(OffboardingInitiationRequest request) {
        AppUser employee = userRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        AppUser manager = userRepo.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        AppUser initiator = currentUserService.getCurrentUser();

        // 1. Create Main Request
        OffboardingRequest offboarding = OffboardingRequest.builder()
                .employee(employee)
                .manager(manager)
                .initiatedBy(initiator)
                .resignationDate(request.getResignationDate())
                .lastWorkingDay(request.getLastWorkingDay())
                .reason(request.getReason())
                .status(OffboardingStatus.IN_PROGRESS)
                .build();
        offboarding = offboardingRepo.save(offboarding);

        // 2. Create Departmental Clearances
        for (ClearanceDepartment dept : ClearanceDepartment.values()) {
            if (dept != ClearanceDepartment.FINANCE) { // Skipping finance for now
                ClearanceChecklist checklist = ClearanceChecklist.builder()
                        .request(offboarding)
                        .department(dept)
                        .status(ClearanceStatus.PENDING)
                        .build();
                clearanceRepo.save(checklist);
            }
        }

        // 3. Identify Assigned Assets (TODO: Adapt to your actual Asset Allocation logic)
        // Since Asset doesn't have 'assignedTo', you likely have an AssetAllocation entity.
        // Example: List<AssetAllocation> allocations = assetAllocationRepo.findByEmployeeAndIsActiveTrue(employee);
        // For now, we skip auto-creating return records, or you can add them manually via UI.
        log.info("✅ Offboarding initiated for {}. (TODO: Link actual assigned assets)", employee.getUsername());

        return offboarding;
    }

    /**
     * 2. SUBMIT CLEARANCE WITH DIGITAL SIGNATURE
     * Updates checklist and checks if the whole offboarding is complete.
     */
    @Transactional
    public ClearanceChecklist submitClearance(ClearanceSubmissionRequest request) {
        ClearanceChecklist checklist = clearanceRepo.findById(request.getClearanceId())
                .orElseThrow(() -> new RuntimeException("Clearance not found"));

        AppUser currentUser = currentUserService.getCurrentUser();

        // Update Checklist
        checklist.setStatus(request.getStatus());
        checklist.setClearedBy(currentUser);
        checklist.setClearedAt(LocalDateTime.now());
        checklist.setRemarks(request.getRemarks());
        checklist.setSignatureData(request.getSignatureData()); // Stores the Base64 signature

        clearanceRepo.save(checklist);

        // Check if all clearances are done
        List<ClearanceChecklist> allChecklists = clearanceRepo.findByRequestId(checklist.getRequest().getId());
        boolean allCleared = allChecklists.stream().allMatch(c -> c.getStatus() == ClearanceStatus.CLEARED);

        if (allCleared) {
            OffboardingRequest completedRequest = checklist.getRequest();
            completedRequest.setStatus(OffboardingStatus.COMPLETED);
            offboardingRepo.save(completedRequest);

            log.info("🎉 Offboarding COMPLETED for {}", completedRequest.getEmployee().getUsername());

            // ✅ TRIGGER THE AUTOMATED CLEANUP (Step 3)
            eventPublisher.publishEvent(new com.sujal.itsm.core.offboarding.event.OffboardingCompletedEvent(this, completedRequest));
        }

        return checklist;
    }

    /**
     * 3. GET DASHBOARD DATA
     */
    public List<OffboardingRequest> getPendingOffboardings() {
        return offboardingRepo.findByStatus(OffboardingStatus.IN_PROGRESS);
    }

    public OffboardingRequest getOffboardingDetails(Long id) {
        return offboardingRepo.findById(id).orElseThrow();
    }

    public List<ClearanceChecklist> getClearancesForRequest(Long requestId) {
        return clearanceRepo.findByRequestId(requestId);
    }

    public List<AssetReturnRecord> getAssetReturnsForRequest(Long requestId) {
        return assetReturnRepo.findByRequestId(requestId);
    }
}