package com.scp.java.ocm.provider.controller;

import com.scp.java.ocm.common.dto.PageResponse;
import com.scp.java.ocm.common.web.PageableHelper;
import com.scp.java.ocm.provider.dto.ProviderRequest;
import com.scp.java.ocm.provider.dto.ProviderResponse;
import com.scp.java.ocm.provider.entity.ProviderSpecialty;
import com.scp.java.ocm.provider.service.ProviderService;
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
@RequestMapping("/api/v1/providers")
public class ProviderController {
    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody ProviderRequest r) {
        ProviderResponse x = service.create(r);
        return ResponseEntity.created(URI.create("/api/v1/providers/" + x.getId())).body(x);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<PageResponse<ProviderResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) ProviderSpecialty specialty,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(
                PageResponse.of(
                        service.findAll(
                                PageableHelper.create(
                                        page,
                                        size,
                                        sort,
                                        "id",
                                        "npi",
                                        "firstName",
                                        "lastName",
                                        "specialty",
                                        "facilityName",
                                        "email",
                                        "active"),
                                specialty,
                                active)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER','VIEWER')")
    public ResponseEntity<ProviderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CARE_MANAGER')")
    public ResponseEntity<ProviderResponse> update(
            @PathVariable Long id, @Valid @RequestBody ProviderRequest r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
