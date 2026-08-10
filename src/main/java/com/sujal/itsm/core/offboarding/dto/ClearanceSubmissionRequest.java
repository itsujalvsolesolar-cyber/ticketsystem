package com.sujal.itsm.core.offboarding.dto;

import com.sujal.itsm.core.offboarding.enums.ClearanceStatus;
import lombok.Data;

@Data
public class ClearanceSubmissionRequest {
    private Long clearanceId; // ✅ Correct spelling
    private ClearanceStatus status;
    private String remarks;
    private String signatureData;
}