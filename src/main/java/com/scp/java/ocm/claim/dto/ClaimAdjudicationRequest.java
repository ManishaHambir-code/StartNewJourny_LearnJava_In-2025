package com.scp.java.ocm.claim.dto;

import com.scp.java.ocm.claim.entity.ClaimStatus;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaimAdjudicationRequest {
    @NotNull private ClaimStatus status;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal allowedAmount;

    @Size(max = 255)
    private String denialReason;
}
