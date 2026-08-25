package com.scp.java.ocm.member.controller;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.scp.java.ocm.claim.dto.ClaimResponse;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.dto.PageResponse;

@RestController
@RequestMapping("/api/v1/members/{memberId}/claims")
public class MemberClaimController {
    private final ClaimService service;
    public MemberClaimController(ClaimService service) { this.service = service; }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<ClaimResponse>> find(@PathVariable Long memberId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="id,asc") String sort) {
        String[] x = sort.split(",", 2);
        Sort.Direction d = x.length > 1 && "desc".equalsIgnoreCase(x[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return ResponseEntity.ok(PageResponse.of(service.findByMember(memberId,
                PageRequest.of(page, size, Sort.by(d, x[0])))));
    }
}
