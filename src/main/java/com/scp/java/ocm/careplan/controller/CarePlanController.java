package com.scp.java.ocm.careplan.controller;

import com.scp.java.ocm.careplan.dto.CarePlanRequest;
import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.entity.ProgramType;
import com.scp.java.ocm.careplan.service.CarePlanService;
import com.scp.java.ocm.common.dto.PageResponse;
import com.scp.java.ocm.common.web.PageableHelper;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/care-plans")
public class CarePlanController {
    private final CarePlanService service;

    public CarePlanController(CarePlanService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> create(@Valid @RequestBody CarePlanRequest r) {
        CarePlanResponse x = service.create(r);
        return ResponseEntity.created(URI.create("/api/v1/care-plans/" + x.getId())).body(x);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<CarePlanResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) CarePlanStatus status,
            @RequestParam(required = false) ProgramType program) {
        return ResponseEntity.ok(
                PageResponse.of(
                        service.findAll(
                                PageableHelper.create(
                                        page,
                                        size,
                                        sort,
                                        "id",
                                        "member",
                                        "careManager",
                                        "program",
                                        "riskLevel",
                                        "status",
                                        "startDate",
                                        "endDate"),
                                memberId,
                                status,
                                program)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<CarePlanResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> update(
            @PathVariable Long id, @Valid @RequestBody CarePlanRequest r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(service.complete(id));
    }
}
