package com.scp.java.ocm.security.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scp.java.ocm.security.dto.LoginRequest;
import com.scp.java.ocm.security.dto.LoginResponse;
import com.scp.java.ocm.security.dto.RegisterUserRequest;
import com.scp.java.ocm.security.dto.UserResponse;
import com.scp.java.ocm.security.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/auth/users/" + created.getId())).body(created);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(UserResponse.from(authService.currentUser(authentication.getName())));
    }
}
