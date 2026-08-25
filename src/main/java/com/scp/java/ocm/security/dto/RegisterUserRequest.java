package com.scp.java.ocm.security.dto;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.scp.java.ocm.security.entity.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class RegisterUserRequest {
    @NotBlank @Size(max = 60) private String username;
    @NotBlank @Size(min = 8, max = 100) private String password;
    @NotBlank @Size(max = 100) private String fullName;
    private Set<Role> roles;
}
