package com.scp.java.ocm.provider.mapper;

import com.scp.java.ocm.provider.dto.ProviderRequest;
import com.scp.java.ocm.provider.dto.ProviderResponse;
import com.scp.java.ocm.provider.entity.Provider;
import org.springframework.stereotype.Component;

@Component
public class ProviderMapper {
    public ProviderResponse toResponse(Provider e) {
        return new ProviderResponse(
                e.getId(),
                e.getNpi(),
                e.getFirstName(),
                e.getLastName(),
                e.getSpecialty(),
                e.getFacilityName(),
                e.getEmail(),
                e.getPhone(),
                e.isActive());
    }

    public Provider toEntity(ProviderRequest r) {
        Provider e = new Provider();
        copy(r, e);
        return e;
    }

    public void copy(ProviderRequest r, Provider e) {
        e.setNpi(r.getNpi());
        e.setFirstName(r.getFirstName());
        e.setLastName(r.getLastName());
        e.setSpecialty(r.getSpecialty());
        e.setFacilityName(r.getFacilityName());
        e.setEmail(r.getEmail());
        e.setPhone(r.getPhone());
        if (r.getActive() != null) e.setActive(r.getActive());
    }
}
