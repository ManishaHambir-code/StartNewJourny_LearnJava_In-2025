package com.scp.java.ocm.provider.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.scp.java.ocm.provider.entity.ProviderSpecialty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ProviderRequest {
    @NotBlank @Pattern(regexp = "\\d{10}") private String npi;
    @NotBlank @Size(max = 50) private String firstName;
    @NotBlank @Size(max = 50) private String lastName;
    @NotNull private ProviderSpecialty specialty;
    @Size(max = 120) private String facilityName;
    @Email @Size(max = 120) private String email;
    @Size(max = 20) private String phone;
    private Boolean active;
}
