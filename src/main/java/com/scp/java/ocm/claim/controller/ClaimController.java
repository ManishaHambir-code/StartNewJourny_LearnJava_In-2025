package com.scp.java.ocm.claim.controller;

import com.scp.java.ocm.claim.dto.ClaimAdjudicationRequest;
import com.scp.java.ocm.claim.dto.ClaimRequest;
import com.scp.java.ocm.claim.dto.ClaimResponse;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.dto.PageResponse;
import com.scp.java.ocm.common.web.PageableHelper;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/claims")
public class ClaimController {
    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimRequest r) {
        ClaimResponse x = service.create(r);
        return ResponseEntity.created(URI.create("/api/v1/claims/" + x.getId())).body(x);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<ClaimResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) ClaimStatus status) {
        return ResponseEntity.ok(
                PageResponse.of(
                        service.findAll(
                                PageableHelper.create(
                                        page,
                                        size,
                                        sort,
                                        "id",
                                        "claimNumber",
                                        "member",
                                        "provider",
                                        "serviceDate",
                                        "billedAmount",
                                        "allowedAmount",
                                        "status"),
                                memberId,
                                providerId,
                                status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<ClaimResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> update(
            @PathVariable Long id, @Valid @RequestBody ClaimRequest r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @PostMapping("/{id}/adjudicate")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> adjudicate(
            @PathVariable Long id, @Valid @RequestBody ClaimAdjudicationRequest r) {
        return ResponseEntity.ok(service.adjudicate(id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
