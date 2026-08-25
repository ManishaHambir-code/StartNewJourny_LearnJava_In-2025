package com.scp.java.ocm.security.service;

import com.scp.java.ocm.security.dto.LoginRequest;
import com.scp.java.ocm.security.dto.LoginResponse;
import com.scp.java.ocm.security.dto.RegisterUserRequest;
import com.scp.java.ocm.security.dto.UserResponse;
import com.scp.java.ocm.security.entity.AppUser;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterUserRequest request);

    AppUser currentUser(String username);
}
