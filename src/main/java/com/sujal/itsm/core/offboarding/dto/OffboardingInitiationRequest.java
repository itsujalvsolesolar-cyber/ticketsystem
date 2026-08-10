package com.sujal.itsm.core.offboarding.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OffboardingInitiationRequest {
    private Long employeeId;
    private Long managerId;
    private LocalDate resignationDate;
    private LocalDate lastWorkingDay;
    private String reason; // e.g., "Resignation", "Termination"
}