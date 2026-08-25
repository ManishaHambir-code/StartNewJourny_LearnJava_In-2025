package com.scp.java.ocm.claim.controller;

import java.net.URI;
import javax.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.scp.java.ocm.claim.dto.*;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.dto.PageResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController @Validated @RequestMapping("/api/v1/claims")
public class ClaimController {
    private final ClaimService service;
    public ClaimController(ClaimService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimRequest r){ClaimResponse x=service.create(r);return ResponseEntity.created(URI.create("/api/v1/claims/"+x.getId())).body(x);}
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<ClaimResponse>> findAll(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size,@RequestParam(defaultValue="id,asc")String sort,@RequestParam(required=false)Long memberId,@RequestParam(required=false)Long providerId,@RequestParam(required=false)ClaimStatus status){return ResponseEntity.ok(PageResponse.of(service.findAll(pageable(page,size,sort),memberId,providerId,status)));}
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<ClaimResponse> findById(@PathVariable Long id){return ResponseEntity.ok(service.findById(id));}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> update(@PathVariable Long id,@Valid @RequestBody ClaimRequest r){return ResponseEntity.ok(service.update(id,r));}
    @PostMapping("/{id}/adjudicate") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ClaimResponse> adjudicate(@PathVariable Long id,@Valid @RequestBody ClaimAdjudicationRequest r){return ResponseEntity.ok(service.adjudicate(id,r));}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}
    private Pageable pageable(int p,int s,String sort){String[] x=sort.split(",",2);Sort.Direction d=x.length>1&&"desc".equalsIgnoreCase(x[1])?Sort.Direction.DESC:Sort.Direction.ASC;return PageRequest.of(p,s,Sort.by(d,x[0]));}
}
