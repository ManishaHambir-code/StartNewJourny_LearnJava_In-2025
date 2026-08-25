package com.scp.java.ocm.careplan.dto;

import java.time.LocalDate;
import com.scp.java.ocm.careplan.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class CarePlanResponse {
    private Long id; private Long memberId; private Long careManagerId; private ProgramType program;
    private RiskLevel riskLevel; private CarePlanStatus status; private String goals;
    private LocalDate startDate; private LocalDate endDate;
}
