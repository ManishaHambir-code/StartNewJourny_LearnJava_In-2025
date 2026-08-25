package com.scp.java.ocm.claim.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ClaimRequest {
    @NotBlank @Size(max=30) private String claimNumber;
    @NotNull private Long memberId;
    @NotNull private Long providerId;
    @NotNull @PastOrPresent private LocalDate serviceDate;
    @NotNull @DecimalMin(value="0.0",inclusive=false) private BigDecimal billedAmount;
    private BigDecimal allowedAmount;
    private String denialReason;
    @NotBlank @Pattern(regexp="[A-Z][0-9]{2}(\\.[0-9A-Z]{1,4})?") private String diagnosisCode;
}
