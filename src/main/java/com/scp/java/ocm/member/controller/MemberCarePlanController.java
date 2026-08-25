package com.scp.java.ocm.member.controller;

import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.service.CarePlanService;
import com.scp.java.ocm.common.dto.PageResponse;
import com.scp.java.ocm.common.web.PageableHelper;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/members/{memberId}/care-plans")
public class MemberCarePlanController {
    private final CarePlanService service;

    public MemberCarePlanController(CarePlanService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<CarePlanResponse>> find(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return ResponseEntity.ok(
                PageResponse.of(
                        service.findByMember(
                                memberId,
                                PageableHelper.create(
                                        page,
                                        size,
                                        sort,
                                        "id",
                                        "program",
                                        "riskLevel",
                                        "status",
                                        "startDate",
                                        "endDate"))));
    }
}
