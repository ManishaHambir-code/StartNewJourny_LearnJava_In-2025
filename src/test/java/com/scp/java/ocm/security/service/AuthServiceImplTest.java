package com.scp.java.ocm.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.security.*;
import com.scp.java.ocm.security.dto.*;
import com.scp.java.ocm.security.entity.*;
import com.scp.java.ocm.security.repository.AppUserRepository;
import com.scp.java.ocm.security.service.impl.AuthServiceImpl;

public class AuthServiceImplTest {
    @Mock AppUserRepository repository; @Mock AuthenticationManager manager; @Mock JwtTokenProvider tokens;
    AuthServiceImpl service;
    @BeforeEach void setUp(){MockitoAnnotations.initMocks(this);service=new AuthServiceImpl(repository,new BCryptPasswordEncoder(),manager,tokens);}
    @Test void duplicateUsernameRejected(){RegisterUserRequest r=new RegisterUserRequest();r.setUsername("admin");when(repository.existsByUsernameIgnoreCase("admin")).thenReturn(true);assertThrows(DuplicateResourceException.class,()->service.register(r));}
    @Test void loginCreatesBearerResponse(){LoginRequest r=new LoginRequest();r.setUsername("admin");r.setPassword("x");Authentication a=new UsernamePasswordAuthenticationToken("admin","x",Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));when(manager.authenticate(any())).thenReturn(a);when(tokens.createToken(a)).thenReturn("jwt");assertEquals("jwt",service.login(r).getAccessToken());}
}
