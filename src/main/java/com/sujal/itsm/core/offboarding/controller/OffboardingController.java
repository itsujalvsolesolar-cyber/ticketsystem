package com.sujal.itsm.core.offboarding.controller;

import com.sujal.itsm.core.offboarding.dto.ClearanceSubmissionRequest;
import com.sujal.itsm.core.offboarding.dto.OffboardingInitiationRequest;
import com.sujal.itsm.core.offboarding.model.ClearanceChecklist;
import com.sujal.itsm.core.offboarding.model.OffboardingRequest;
import com.sujal.itsm.core.offboarding.service.OffboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/offboarding")
@RequiredArgsConstructor
public class OffboardingController {

    private final OffboardingService offboardingService;

    @PostMapping("/initiate")
    public ResponseEntity<OffboardingRequest> initiate(@RequestBody OffboardingInitiationRequest request) {
        return ResponseEntity.ok(offboardingService.initiateOffboarding(request));
    }

    @PostMapping("/clearance/submit")
    public ResponseEntity<ClearanceChecklist> submitClearance(@RequestBody ClearanceSubmissionRequest request) {
        return ResponseEntity.ok(offboardingService.submitClearance(request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OffboardingRequest>> getPending() {
        return ResponseEntity.ok(offboardingService.getPendingOffboardings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffboardingRequest> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(offboardingService.getOffboardingDetails(id));
    }
}