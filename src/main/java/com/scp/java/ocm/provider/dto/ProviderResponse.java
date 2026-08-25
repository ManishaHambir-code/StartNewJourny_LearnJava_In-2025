package com.scp.java.ocm.provider.dto;

import com.scp.java.ocm.provider.entity.ProviderSpecialty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ProviderResponse {
    private Long id; private String npi; private String firstName; private String lastName;
    private ProviderSpecialty specialty; private String facilityName; private String email; private String phone;
    private boolean active;
}
