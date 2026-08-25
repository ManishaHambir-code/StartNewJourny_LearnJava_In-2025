package com.scp.java.ocm.member.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.scp.java.ocm.common.dto.PageResponse;
import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.dto.MemberResponse;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.service.MemberService;

@RestController @Validated @RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberService service;
    public MemberController(MemberService service) { this.service = service; }

    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/members/" + response.getId())).body(response);
    }

    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<MemberResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort, @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) String lastName, @RequestParam(required = false) String planId) {
        return ResponseEntity.ok(PageResponse.of(service.findAll(pageable(page, size, sort), status, lastName, planId)));
    }

    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<MemberResponse> findById(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }

    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<MemberResponse> status(@PathVariable Long id, @RequestParam MemberStatus status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    private Pageable pageable(int page, int size, String sort) {
        String[] s = sort.split(",", 2);
        Sort.Direction direction = s.length > 1 && "desc".equalsIgnoreCase(s[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, s[0]));
    }
}
