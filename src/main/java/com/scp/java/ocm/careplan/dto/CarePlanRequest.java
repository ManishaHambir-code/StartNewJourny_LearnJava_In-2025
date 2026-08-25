package com.scp.java.ocm.careplan.dto;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.scp.java.ocm.careplan.entity.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CarePlanRequest {
    @NotNull private Long memberId;
    @NotNull private Long careManagerId;
    @NotNull private ProgramType program;
    @NotNull private RiskLevel riskLevel;
    private CarePlanStatus status;
    @Size(max=2000) private String goals;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
}
