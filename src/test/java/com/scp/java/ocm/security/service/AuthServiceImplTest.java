package com.scp.java.ocm.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.security.JwtProperties;
import com.scp.java.ocm.security.JwtTokenProvider;
import com.scp.java.ocm.security.dto.LoginRequest;
import com.scp.java.ocm.security.dto.RegisterUserRequest;
import com.scp.java.ocm.security.repository.AppUserRepository;
import com.scp.java.ocm.security.service.impl.AuthServiceImpl;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock AppUserRepository repository;
    @Mock AuthenticationManager manager;
    @Mock JwtTokenProvider tokens;
    AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        service =
                new AuthServiceImpl(repository, new BCryptPasswordEncoder(), manager, tokens, properties);
    }

    @Test
    void duplicateUsernameRejected() {
        RegisterUserRequest r = new RegisterUserRequest();
        r.setUsername("admin");
        when(repository.existsByUsernameIgnoreCase("admin")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.register(r));
    }

    @Test
    void loginCreatesBearerResponse() {
        LoginRequest r = new LoginRequest();
        r.setUsername("admin");
        r.setPassword("x");
        Authentication a =
                new UsernamePasswordAuthenticationToken(
                        "admin", "x", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(manager.authenticate(any())).thenReturn(a);
        when(tokens.createToken(a)).thenReturn("jwt");
        assertEquals("jwt", service.login(r).getAccessToken());
    }
}
