package com.scp.java.ocm.security.dto;

import com.scp.java.ocm.security.entity.AppUser;
import com.scp.java.ocm.security.entity.Role;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private Set<Role> roles;
    private boolean enabled;

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getFullName(), user.getRoles(), user.isEnabled());
    }
}
