package com.scp.java.ocm.provider.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.scp.java.ocm.provider.dto.ProviderRequest;
import com.scp.java.ocm.provider.dto.ProviderResponse;
import com.scp.java.ocm.provider.entity.ProviderSpecialty;

public interface ProviderService {
    ProviderResponse create(ProviderRequest request);
    Page<ProviderResponse> findAll(Pageable pageable, ProviderSpecialty specialty, Boolean active);
    ProviderResponse findById(Long id);
    ProviderResponse update(Long id, ProviderRequest request);
    void delete(Long id);
}
