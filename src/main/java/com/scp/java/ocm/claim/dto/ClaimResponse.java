package com.scp.java.ocm.claim.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ClaimResponse {
    private Long id; private String claimNumber; private Long memberId; private Long providerId;
    private LocalDate serviceDate; private BigDecimal billedAmount; private BigDecimal allowedAmount;
    private ClaimStatus status; private String denialReason; private String diagnosisCode;
}
