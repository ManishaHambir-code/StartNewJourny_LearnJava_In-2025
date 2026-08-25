package com.scp.java.ocm.careplan.controller;

import java.net.URI;
import javax.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.scp.java.ocm.careplan.dto.*;
import com.scp.java.ocm.careplan.entity.*;
import com.scp.java.ocm.careplan.service.CarePlanService;
import com.scp.java.ocm.common.dto.PageResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController @Validated @RequestMapping("/api/v1/care-plans")
public class CarePlanController {
    private final CarePlanService service;
    public CarePlanController(CarePlanService service){this.service=service;}
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> create(@Valid @RequestBody CarePlanRequest r){CarePlanResponse x=service.create(r);return ResponseEntity.created(URI.create("/api/v1/care-plans/"+x.getId())).body(x);}
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<CarePlanResponse>> findAll(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size,@RequestParam(defaultValue="id,asc")String sort,@RequestParam(required=false)Long memberId,@RequestParam(required=false)CarePlanStatus status,@RequestParam(required=false)ProgramType program){return ResponseEntity.ok(PageResponse.of(service.findAll(pageable(page,size,sort),memberId,status,program)));}
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<CarePlanResponse> findById(@PathVariable Long id){return ResponseEntity.ok(service.findById(id));}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> update(@PathVariable Long id,@Valid @RequestBody CarePlanRequest r){return ResponseEntity.ok(service.update(id,r));}
    @PostMapping("/{id}/activate") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> activate(@PathVariable Long id){return ResponseEntity.ok(service.activate(id));}
    @PostMapping("/{id}/complete") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<CarePlanResponse> complete(@PathVariable Long id){return ResponseEntity.ok(service.complete(id));}
    private Pageable pageable(int p,int s,String sort){String[] x=sort.split(",",2);Sort.Direction d=x.length>1&&"desc".equalsIgnoreCase(x[1])?Sort.Direction.DESC:Sort.Direction.ASC;return PageRequest.of(p,s,Sort.by(d,x[0]));}
}
