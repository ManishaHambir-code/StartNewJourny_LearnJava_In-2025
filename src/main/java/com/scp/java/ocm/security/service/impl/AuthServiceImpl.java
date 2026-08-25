package com.scp.java.ocm.security.service.impl;

import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.common.exception.ResourceNotFoundException;
import com.scp.java.ocm.security.JwtProperties;
import com.scp.java.ocm.security.JwtTokenProvider;
import com.scp.java.ocm.security.dto.LoginRequest;
import com.scp.java.ocm.security.dto.LoginResponse;
import com.scp.java.ocm.security.dto.RegisterUserRequest;
import com.scp.java.ocm.security.dto.UserResponse;
import com.scp.java.ocm.security.entity.AppUser;
import com.scp.java.ocm.security.entity.Role;
import com.scp.java.ocm.security.repository.AppUserRepository;
import com.scp.java.ocm.security.service.AuthService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(
            AppUserRepository repository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            JwtProperties jwtProperties) {
        this.repository = repository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = tokenProvider.createToken(authentication);
        List<String> roles = new ArrayList<String>();
        authentication
                .getAuthorities()
                .forEach(a -> roles.add(a.getAuthority().replaceFirst("^ROLE_", "")));
        return new LoginResponse(
                token, "Bearer", jwtProperties.getExpirationMs(), authentication.getName(), roles);
    }

    @Override
    public UserResponse register(RegisterUserRequest request) {
        if (repository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        Set<Role> roles =
                request.getRoles() == null || request.getRoles().isEmpty()
                        ? new HashSet<Role>(Collections.singleton(Role.VIEWER))
                        : request.getRoles();
        AppUser user =
                new AppUser(
                        request.getUsername(),
                        encoder.encode(request.getPassword()),
                        request.getFullName(),
                        roles);
        return UserResponse.from(repository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser currentUser(String username) {
        return repository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
